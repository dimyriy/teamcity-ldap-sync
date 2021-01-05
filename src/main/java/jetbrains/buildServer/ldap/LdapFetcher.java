/*
 * Copyright 2000-2021 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.buildServer.ldap;

import jetbrains.buildServer.ldap.api.LdapContext;
import jetbrains.buildServer.ldap.api.LdapExecutor;
import jetbrains.buildServer.ldap.api.LdapSearchUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import javax.naming.directory.Attribute;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static jetbrains.buildServer.ldap.api.PropertiesConstants.*;

public class LdapFetcher {
    @NonNull
    public static final Logger LOGGER = LogManager.getLogger(LdapFetcher.class);
    @NonNull
    private final LdapProperties ldapProperties;
    @NonNull
    private final RemoteDataFetcherOptions dataFetcherOptions;

    public LdapFetcher(@NonNull final RemoteDataFetcherOptions dataFetcherOptions, @NonNull final LdapProperties ldapProperties) {
        this.ldapProperties = ldapProperties;
        this.dataFetcherOptions = dataFetcherOptions;
    }

    @Nullable
    public static String addParenthesesIfNeeded(@Nullable String s) {
        if (s != null && s.length() > 0 && !(s.startsWith("(") && s.endsWith(")"))) {
            return "(" + s + ")";
        }
        return s;
    }

    @NonNull
    String getRemoteId(@NonNull final SearchResult searchResult) {
        final String attributeName = ldapProperties.getProperty(KEY_MEMBER_USER_ID_ATTRIBUTE);
        if (StringUtils.hasText(attributeName)) {
            return getAttributeBasedValue(searchResult, KEY_MEMBER_USER_ID_ATTRIBUTE);
        }
        return getDn(searchResult);
    }

    @NonNull
    String getDn(@NonNull final SearchResult searchResult) {
        try {
            return searchResult.getNameInNamespace();
        } catch (UnsupportedOperationException e) {
            if (!searchResult.isRelative()) {
                return searchResult.getName();
            }
            LOGGER.error("Trying to get DN for LDAP entry " + searchResult.toString() + " via attribute, as there was an error retrieving DN via API: " + e.toString());
            return getAttributeBasedValue(searchResult, KEY_DN_ATTR);
        }
    }

    @Nullable
    public RemoteData fetchAllRemoteData() {
        final AtomicReference<RemoteData> remoteData = new AtomicReference<>();
        try {
            final LdapContext ldapContext = new LdapContext(ldapProperties);
            ldapContext.runLdapCommands(executor -> {
                if (dataFetcherOptions.isUserSyncEnabled()) {
                    try {
                        remoteData.set(new RemoteData(fillUserData(executor)));
                    } catch (Exception e) {
                        LOGGER.error("Error while retrieving LDAP users, skipping users synchronization", e);
                    }
                } else {
                    LOGGER.error("Skipping users synchronization as '{}' property is not set to '{}'", KEY_ENABLE_USER_SYNC_OPTION, true);
                }
            });
        } catch (Exception e) {
            LOGGER.error("Error while initializing LDAP connection", e);
        }
        return remoteData.get();
    }

    @NonNull
    private List<RemoteUserData> fillUserData(@NonNull final LdapExecutor executor) {
        List<SearchResult> userResults = fetchUsers(executor);

        final List<RemoteUserData> result = new ArrayList<>(userResults.size());
        for (SearchResult searchResult : userResults) {
            try {
                result.add(convertToRemoteUserData(searchResult));
            } catch (Exception e) {
                LOGGER.error("Error while retrieving LDAP user information", e);
            }
        }
        LOGGER.info("Got " + result.size() + " users from LDAP");
        return result;
    }

    @NonNull
    private List<SearchResult> fetchUsers(@NonNull final LdapExecutor executor) {
        final String userBaseProperty = getUserBaseProperty();
        final String userFilterProperty = getUserFilterProperty();
        return executor.search(userBaseProperty, userFilterProperty, LdapSearchUtil.getDefaultSearchControls(ldapProperties.getUserAttributesToRetrieve()));
    }

    @NonNull
    private RemoteUserData convertToRemoteUserData(@NonNull SearchResult searchResult) {
        final String remoteId = getRemoteId(searchResult);

        String username = getAttributeBasedValue(searchResult, KEY_USER_USERNAME_ATTR).toLowerCase();
        String transformKey = "teamcity.users.username.transform";
        String usernameTransform = ldapProperties.getProperty(transformKey);
        if (usernameTransform != null) {
            String transformed = usernameTransform.replace("$username$", username);
            LOGGER.info("Username transformed from '" + username + "' to '" + transformed + "' " +
                    "due to setting: " + transformKey + "=" +
                    usernameTransform);
            username = transformed;
        }
        String displayName = getAttributeBasedValueForConfigParam(searchResult, KEY_USER_DISPLAY_NAME_ATTR);

        String email = getAttributeBasedValueForConfigParam(searchResult, KEY_USER_MAIL_ATTR);

        Map<String, String> customProperties = new HashMap<>();
        for (Map.Entry<String, String> entry : ldapProperties.getCustomProperties().entrySet()) {
            String customProperty = entry.getKey();
            String attributeName = entry.getValue();
            try {
                final String customPropertyValue = getAttributeBasedValueForConfigParam(searchResult, KEY_USER_CUSTOM_PROPERTY + customProperty);
                customProperties.put(customProperty, customPropertyValue);
            } catch (Exception e) {
                LOGGER.error("Cannot retrieve attribute or resolve value for custom user property '" + KEY_USER_CUSTOM_PROPERTY + customProperty +
                        "' defined to '" + attributeName + "' , " + e.toString());
            }
        }

        return new RemoteUserData(remoteId, username, displayName, email, customProperties);
    }

    @NonNull
    private String getAttributeBasedValueForConfigParam(@NonNull final SearchResult searchResult, final String configParam) {
        final String attributeName = ldapProperties.getProperty(configParam);
        if (attributeName == null) {
            throw new IllegalArgumentException("Config does not contain parameter '" + configParam + "'");
        }
        return getAttributeBasedValue(searchResult, attributeName);
    }

    @NonNull
    private String getAttributeBasedValue(@NonNull final SearchResult searchResult,
                                          @NonNull final String attributeName) {
        try {
            Attribute value = searchResult.getAttributes().get(attributeName);
            if (value == null) {
                throw new IllegalArgumentException("No such attribute " + attributeName + " exists in search result " + searchResult);
            }
            final Object attrValue = value.get();
            if (attrValue == null) {
                throw new IllegalArgumentException("Attribute " + attributeName + " has null value for search result " + searchResult);
            }
            return attrValue.toString();
        } catch (Exception e) {
            LOGGER.error("Got exception while getting value of attribute {} for search result {}", attributeName, searchResult, e);
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @NonNull
    private String getUserBaseProperty() {
        final String result = ldapProperties.getProperty(KEY_USER_BASE);
        if (result == null) {
            throw new IllegalArgumentException("Cannot search users in LDAP as '" + KEY_USER_BASE + "' property is not set");
        }
        return result;
    }

    @NonNull
    private String getUserFilterProperty() {
        final String result = getUserFilterPropertyIfDefined();
        if (!StringUtils.hasText(result)) {
            throw new IllegalArgumentException("Cannot search users in LDAP as '" + KEY_USER_SEARCH_FILTER + "' property is not set or empty");
        }
        return result;
    }

    @Nullable
    private String getUserFilterPropertyIfDefined() {
        return addParenthesesIfNeeded(ldapProperties.getProperty(KEY_USER_SEARCH_FILTER));
    }
}

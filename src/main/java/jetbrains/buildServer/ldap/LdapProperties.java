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

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.ldap.core.support.AbstractContextSource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.util.StringUtils;

import javax.naming.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static jetbrains.buildServer.ldap.api.PropertiesConstants.*;

public class LdapProperties {
    private static final Map<String, String> DEFAULT_PROPERTIES = new HashMap<>();

    static {
        DEFAULT_PROPERTIES.put(KEY_DN_ATTR, "distinguishedName");
        DEFAULT_PROPERTIES.put(KEY_USER_BASE, "");
        DEFAULT_PROPERTIES.put(KEY_GROUP_BASE, "");
    }

    protected Properties properties;
    private Map<String, String> customProperties;

    public LdapProperties() {
        super();
    }

    @NonNull
    private static Map<String, String> getBaseEnvironment(@NonNull final LdapProperties ldapProperties) {
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<Object, Object> entry : ldapProperties.entrySet()) {
            String key = entry.getKey().toString();
            if (!key.startsWith("teamcity.") && !key.equals(Context.SECURITY_CREDENTIALS)) {
                map.put(key, entry.getValue().toString().trim());
            }
        }

        ldapProperties.processBaseEnvironment(map);
        map.remove(Context.PROVIDER_URL);
        map.remove(Context.SECURITY_PRINCIPAL);
        map.remove(Context.SECURITY_CREDENTIALS);
        return map;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void initLdapContextSource(@NonNull final LdapProperties properties, @NonNull LdapContextSource contextSource) {
        String url = properties.getProperty(Context.PROVIDER_URL);
        if (!StringUtils.hasText(url)) {
            throw new IllegalArgumentException("Empty '" + Context.PROVIDER_URL + "' property");
        }
        contextSource.setUrls(url.split("\\s"));

        String principal = properties.getProperty(Context.SECURITY_PRINCIPAL);
        if (StringUtils.hasText(principal)) {
            String credentials = properties.getProperty(Context.SECURITY_CREDENTIALS, "");
            contextSource.setUserDn(principal);
            contextSource.setPassword(credentials);
            contextSource.setAnonymousReadOnly(false);
        } else {
            contextSource.setAnonymousReadOnly(true);
        }

        String contextFactory = properties.getProperty(Context.INITIAL_CONTEXT_FACTORY);
        if (contextFactory != null) {
            try {
                Class<?> clazz = Class.forName(contextFactory);
                contextSource.setContextFactory(clazz);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("getting factory '" + contextFactory + "'", e);
            }
        }

        contextSource.setBaseEnvironmentProperties((Map) LdapProperties.getBaseEnvironment(properties));

        boolean pooled = properties.getBoolean(AbstractContextSource.SUN_LDAP_POOLING_FLAG);
        contextSource.setPooled(pooled);

        try {
            contextSource.afterPropertiesSet();
        } catch (Exception e) {
            throw new IllegalStateException("initializing", e);
        }
    }

    @NonNull
    public synchronized Set<Map.Entry<Object, Object>> entrySet() {
        return properties.entrySet();
    }

    public synchronized String get(@NonNull String key) {
        return getProperty(key);
    }

    public synchronized String getProperty(@NonNull String key, @NonNull String defaultValue) {
        String val = getProperty(key);
        return StringUtils.trimAllWhitespace((val == null) ? defaultValue : val);
    }

    public boolean getBoolean(@NonNull String key) {
        return Boolean.parseBoolean(getProperty(key));
    }

    public synchronized void readFromFile(@NonNull File propertiesFile) throws IOException {
        customProperties = null;
        properties = new Properties();
        try (FileInputStream in = new FileInputStream(propertiesFile)) {
            properties.load(in);
        }
    }

    protected void processBaseEnvironment(@NonNull final Map<String, String> map) {
        map.putIfAbsent(Context.REFERRAL, "follow");
    }

    public synchronized Map<String, String> getCustomProperties() {
        if (customProperties == null) {
            readCustomProperties();
        }
        return customProperties;
    }

    @Nullable
    String[] getUserAttributesToRetrieve() {
        String resolveUserProperties = properties.getProperty(TEAMCITY_USERS_PROPERTIES_RESOLVE);
        if (Boolean.parseBoolean(resolveUserProperties)) {
            return null;
        }
        return getAllAttributes();
    }

    @NonNull
    public String[] getAllAttributes() {
        Set<String> all = new HashSet<>();
        all.add(getProperty(KEY_DN_ATTR, ""));
        all.add(getProperty(KEY_USER_USERNAME_ATTR, ""));
        all.add(getProperty(KEY_USER_MAIL_ATTR, ""));
        all.add(getProperty(KEY_USER_DISPLAY_NAME_ATTR, ""));
        all.add(getProperty(KEY_GROUP_MEMBER_ATTRIBUTE, ""));
        all.add(getProperty(KEY_MEMBER_USER_ID_ATTRIBUTE, ""));
        all.add(getProperty(KEY_MEMBER_GROUP_ID_ATTRIBUTE, ""));
        all.addAll(getCustomProperties().values());
        all.remove("");
        return all.toArray(new String[0]);
    }

    private void readCustomProperties() {
        customProperties = new HashMap<>();
        for (Map.Entry<Object, Object> entry : entrySet()) {
            String key = entry.getKey().toString();
            if (key.startsWith(KEY_USER_CUSTOM_PROPERTY) &&
                    !key.equals(KEY_USER_MAIL_ATTR) &&
                    !key.equals(KEY_USER_DISPLAY_NAME_ATTR)) {
                customProperties.put(key.substring(KEY_USER_CUSTOM_PROPERTY.length()),
                        entry.getValue().toString().trim());
            }
        }
    }

    @NonNull
    public synchronized Map<String, String> getAllProperties() {
        final Map<String, String> result = new HashMap<>(DEFAULT_PROPERTIES);
        properties.forEach((key, value) -> result.put((String) key, (String) value));
        return result;
    }

    @Nullable
    public synchronized String getProperty(@NonNull String key) {
        return (String) properties.getOrDefault(key, DEFAULT_PROPERTIES.get(key));
    }
}
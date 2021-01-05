/*
 * Copyright (c) 2000-2013 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package jetbrains.buildServer.ldap.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.ldap.LimitExceededException;
import org.springframework.ldap.core.CollectingNameClassPairCallbackHandler;
import org.springframework.ldap.core.LdapOperations;

import javax.naming.NameClassPair;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.Arrays;
import java.util.List;

public abstract class LdapSearchUtil {
    @NonNull
    private static final Logger LOGGER = LogManager.getLogger(LdapSearchUtil.class);

    public static List<SearchResult> search(@NonNull final LdapOperations template,
                                            @NonNull final String base,
                                            @NonNull final String filter,
                                            @NonNull final SearchControls searchControls) {
        final CollectingNameClassPairCallbackHandler<SearchResult> callback = new CollectingNameClassPairCallbackHandler<SearchResult>() {
            @Override
            public SearchResult getObjectFromNameClassPair(NameClassPair nameClassPair) {
                String dn = "";
                try {
                    dn = ". Distinguished name: " + nameClassPair.getNameInNamespace();
                } catch (UnsupportedOperationException e) {
                    LOGGER.error("Got exception while trying to get dn from " + nameClassPair, e);
                }
                LOGGER.info("LDAP search result: " + nameClassPair.toString() + dn);
                return (SearchResult) nameClassPair;
            }
        };

        try {
            try {
                final String searchDescription = getSearchDescription(base, filter, searchControls);
                LOGGER.info("Starting to search " + searchDescription);
                template.search(base, filter, searchControls, callback);
                LOGGER.info("Found " + callback.getList().size() + " search results for search " + searchDescription);
            } catch (LimitExceededException e) {
                LOGGER.error(e);
                throw e;
            }
        } catch (Exception e) {
            LOGGER.error(e);
            throw e;
        }
        return callback.getList();
    }

    @NonNull
    public static SearchControls getDefaultSearchControls(final @Nullable String[] attributes) {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setReturningAttributes(attributes);
        return searchControls;
    }

    @NonNull
    private static String getSearchDescription(final String base,
                                               final String filter,
                                               final SearchControls controls) {
        final String[] returningAttributes = controls.getReturningAttributes();
        return "base='" + base + "'" +
                ", filter='" + filter + "'" +
                ", scope=" + controls.getSearchScope() +
                (returningAttributes != null ? ", attributes=" + Arrays.toString(returningAttributes) : "<all>");
    }
}

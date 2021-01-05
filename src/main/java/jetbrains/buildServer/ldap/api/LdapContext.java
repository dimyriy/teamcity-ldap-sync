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
package jetbrains.buildServer.ldap.api;

import jetbrains.buildServer.ldap.LdapProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.NonNull;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.core.support.SingleContextSource;

public class LdapContext {
    @NonNull
    private static final Logger LOGGER = LogManager.getLogger(LdapContext.class);
    @NonNull
    private final LdapProperties ldapProperties;
    @NonNull
    private final LdapContextSource ldapContextSource = new LdapContextSource();


    public LdapContext(@NonNull final LdapProperties properties) {
        ldapProperties = properties;
    }

    public void runLdapCommands(@NonNull final LdapContext.LdapExecutorCallback callback) {
        final LdapContextSource contextSource = getContextSource();
        callback.doWithLdapExecutor((baseDN, filter, searchControls) -> {
            final SingleContextSource singleContextSource;
            singleContextSource = new SingleContextSource(contextSource.getReadOnlyContext());
            try {
                LdapTemplate newLdapTemplate = createNewLdapTemplate(singleContextSource);
                return LdapSearchUtil.search(newLdapTemplate, baseDN, filter, searchControls);
            } finally {
                singleContextSource.destroy();
            }
        });
    }

    private LdapContextSource getContextSource() {
        LdapProperties.initLdapContextSource(ldapProperties, ldapContextSource);
        return ldapContextSource;
    }

    private LdapTemplate createNewLdapTemplate(final ContextSource contextSource) {
        LOGGER.info("Creating new LdapTemplate from " + contextSource.toString());
        return new LdapTemplate(contextSource);
    }

    public interface LdapExecutorCallback {
        void doWithLdapExecutor(@NonNull LdapExecutor executor);
    }
}

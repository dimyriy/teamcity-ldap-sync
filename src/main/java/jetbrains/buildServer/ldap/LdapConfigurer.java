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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.lang.NonNull;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.nio.file.Files;

@Configuration
@PropertySource("classpath:ldap-config.properties")
public class LdapConfigurer {
    @NonNull
    private static final Logger LOGGER = LogManager.getLogger(LdapConfigurer.class);

    @Bean
    public RemoteDataFetcherOptions remoteDataFetcherOptions(@Value("${teamcity.options.users.synchronize}") final boolean synchronizeUsers) {
        return new RemoteDataFetcherOptions(synchronizeUsers);
    }

    @Bean
    public LdapProperties ldapProperties(@Value("${configurationFile:classpath:ldap-config.properties}") @NonNull final String propertiesFile) throws IOException {
        LOGGER.info("Loading properties from file {}", propertiesFile);
        LOGGER.debug("{} content: [\n{}\n]", () -> propertiesFile, () -> {
            try {
                return new String(Files.readAllBytes(ResourceUtils.getFile(propertiesFile).toPath()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        final LdapProperties ldapProperties = new LdapProperties();
        ldapProperties.readFromFile(ResourceUtils.getFile(propertiesFile));
        return ldapProperties;
    }

    @Autowired
    @Bean
    public LdapFetcher ldapFetcher(@NonNull final RemoteDataFetcherOptions dataFetcherOptions, @NonNull final LdapProperties ldapProperties) {
        return new LdapFetcher(dataFetcherOptions, ldapProperties);
    }

    @Autowired
    @Bean
    public LdapSynchronizer ldapSynchronizer(@NonNull final LdapFetcher ldapFetcher) {
        final LdapSynchronizer ldapSynchronizer = new LdapSynchronizer(ldapFetcher);
        LOGGER.info("Sync: " + ldapSynchronizer.sync());
        return ldapSynchronizer;
    }
}

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

public class LdapSynchronizer {
    @NonNull
    private final LdapFetcher ldapFetcher;

    public LdapSynchronizer(@NonNull final LdapFetcher ldapFetcher) {
        this.ldapFetcher = ldapFetcher;
    }

    @Nullable
    public RemoteData sync() {
        return ldapFetcher.fetchAllRemoteData();
    }
}

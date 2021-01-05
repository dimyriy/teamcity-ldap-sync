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

public class RemoteDataFetcherOptions {
    private final boolean userSyncEnabled;

    public RemoteDataFetcherOptions(final boolean synchronizeUsers) {
        this.userSyncEnabled = synchronizeUsers;
    }

    public boolean isUserSyncEnabled() {
        return userSyncEnabled;
    }
}
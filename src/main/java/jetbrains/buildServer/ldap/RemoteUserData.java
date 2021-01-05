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

import java.util.Map;

public class RemoteUserData {
    @NonNull
    private final String remoteId;
    @NonNull
    private final String username;
    @Nullable
    private final String displayName;
    @Nullable
    private final String email;
    @NonNull
    private final Map<String, String> customProperties;

    public RemoteUserData(@NonNull String remoteId,
                          @NonNull String username,
                          @Nullable String displayName,
                          @Nullable String email,
                          @NonNull Map<String, String> customProperties) {
        this.remoteId = remoteId;
        this.username = username;
        this.displayName = displayName;
        this.email = email;
        this.customProperties = customProperties;
    }

    @Override
    public String toString() {
        return "RemoteUserData{" +
                "remoteId='" + remoteId + '\'' +
                ", username='" + username + '\'' +
                ", displayName='" + displayName + '\'' +
                ", email='" + email + '\'' +
                ", customProperties=" + customProperties +
                '}';
    }
}
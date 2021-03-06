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

import java.util.Collection;

public class RemoteData {
    @NonNull
    private final Collection<RemoteUserData> userData;

    public RemoteData(@NonNull final Collection<RemoteUserData> userData) {
        this.userData = userData;
    }

    @Override
    public String toString() {
        return "RemoteData{" +
                "userData=" + userData +
                '}';
    }
}
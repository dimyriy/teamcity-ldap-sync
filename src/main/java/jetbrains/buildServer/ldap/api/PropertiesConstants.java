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

import org.springframework.lang.NonNull;

public interface PropertiesConstants {
    @NonNull
    String KEY_SYNC_OPTION_PREFIX = "teamcity.options.";
    @NonNull
    String KEY_ENABLE_USER_SYNC_OPTION = KEY_SYNC_OPTION_PREFIX + "users.synchronize";
    @NonNull
    String KEY_ENABLE_GROUP_SYNC_OPTION = KEY_SYNC_OPTION_PREFIX + "groups.synchronize";
    @NonNull
    String KEY_CREATE_USERS_SYNC_OPTION = KEY_SYNC_OPTION_PREFIX + "createUsers";
    @NonNull
    String KEY_DELETE_USERS_SYNC_OPTION = KEY_SYNC_OPTION_PREFIX + "deleteUsers";
    @NonNull
    String KEY_ASSIGN_USERS_SYNC_OPTION = KEY_SYNC_OPTION_PREFIX + "assignUsers";
    @NonNull
    String KEY_DEASSIGN_USERS_SYNC_OPTION = KEY_SYNC_OPTION_PREFIX + "deassignUsers";
    @NonNull
    String KEY_CREATE_GROUPS_SYNC_OPTION = KEY_SYNC_OPTION_PREFIX + "createGroups";
    @NonNull
    String KEY_DELETE_GROUPS_SYNC_OPTION = KEY_SYNC_OPTION_PREFIX + "deleteGroups";
    @NonNull
    String KEY_ADD_SUBGROUPS_SYNC_OPTION = KEY_SYNC_OPTION_PREFIX + "addSubgroups";
    @NonNull
    String KEY_REMOVE_SUBGROUPS_SYNC_OPTION = KEY_SYNC_OPTION_PREFIX + "removeSubgroups";
    @NonNull
    String KEY_TIMEOUT_SYNC_OPTION = KEY_SYNC_OPTION_PREFIX + "syncTimeout";
    @NonNull
    String KEY_FORCE_UPDATE_PROPERTIES = "teamcity.users.forceUpdatePropertiesDuringSync";
    @NonNull
    String KEY_CREATE_USERS_ONLY_WHEN_GROUP_SYNC = "teamcity.options.users.synchronize.createUsers";
    @NonNull
    String KEY_DELETE_USERS_ONLY_WHEN_GROUP_SYNC = "teamcity.options.users.synchronize.deleteUsers";
    @NonNull
    String KEY_DN_ATTR = "teamcity.property.distinguishedName";
    @NonNull
    String KEY_USER_BASE = "teamcity.users.base";
    @NonNull
    String KEY_USER_SEARCH_FILTER = "teamcity.users.filter";
    @NonNull
    String KEY_USER_USERNAME_ATTR = "teamcity.users.username";
    @NonNull
    String KEY_USER_CUSTOM_PROPERTY = "teamcity.users.property.";
    @NonNull
    String KEY_USER_DISPLAY_NAME_ATTR = KEY_USER_CUSTOM_PROPERTY + "displayName";
    @NonNull
    String KEY_USER_MAIL_ATTR = KEY_USER_CUSTOM_PROPERTY + "email";
    @NonNull
    String KEY_GROUP_BASE = "teamcity.groups.base";
    @NonNull
    String KEY_GROUP_MEMBER_ATTRIBUTE = "teamcity.groups.property.member";
    @NonNull
    String KEY_MEMBER_USER_ID_ATTRIBUTE = "teamcity.users.property.memberId";
    @NonNull
    String KEY_MEMBER_GROUP_ID_ATTRIBUTE = "teamcity.groups.property.memberId";
    @NonNull
    String TEAMCITY_USERS_PROPERTIES_RESOLVE = "teamcity.users.properties.resolve";
}

/*
 * This file is part of Dependency-Track.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) OWASP Foundation. All Rights Reserved.
 */
package org.dependencytrack.model;

import alpine.model.LdapUser;
import alpine.model.ManagedUser;
import alpine.model.OidcUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import java.io.Serializable;
import java.util.Set;

/**
 * Model for tracking mapped roles. Mapped Roles contain information
 * about an 'assigned' Role and the Mapping of permissions to users
 * within a project.
 *
 * @author Allen Shearin
 * @since 5.6.0
 */
@PersistenceCapable
@FetchGroups({
        @FetchGroup(name = "ALL", members = {
                @Persistent(name = "role"),
                @Persistent(name = "project"),
                @Persistent(name = "permissions"),
                @Persistent(name = "ldapUsers"),
                @Persistent(name = "managedUsers"),
                @Persistent(name = "oidcUsers")
        })
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MappedRole implements Serializable {

    private static final long serialVersionUID = -7592438796591673355L;

    /**
     * Defines JDO fetch groups for this class.
     */
    public enum FetchGroup {
        ALL
    }

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.NATIVE)
    @JsonIgnore
    private long id;

    @Persistent
    private Role role;

    @Persistent
    private Project project;

    @Persistent
    private Set<LdapUser> ldapUsers;
    
    @Persistent
    private Set<ManagedUser> managedUsers;
    
    @Persistent
    private Set<OidcUser> oidcUsers;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Set<LdapUser> getLdapUsers() {
        return ldapUsers;
    }

    public void setLdapUsers(Set<LdapUser> ldapUsers) {
        this.ldapUsers = ldapUsers;
    }

    public void addLdapUser(LdapUser ldapuser) {
        this.ldapUsers.add(ldapuser);
    }
    
    public void removeLdapUser(LdapUser ldapuser) {
        this.ldapUsers.remove(ldapuser);
    }

    public Set<ManagedUser> getManagedUsers() {
        return managedUsers;
    }

    public void setManagedUsers(Set<ManagedUser> managedUsers) {
        this.managedUsers = managedUsers;
    }

    public void addManagedUser(ManagedUser manageduser) {
        this.managedUsers.add(manageduser);
    }
    
    public void removeManagedUser(ManagedUser manageduser) {
        this.managedUsers.remove(manageduser);
    }

    public Set<OidcUser> getOidcUsers() {
        return oidcUsers;
    }

    public void setOidcUsers(Set<OidcUser> oidcUsers) {
        this.oidcUsers = oidcUsers;
    }
    
    public void addOidcUser(OidcUser oidcuser) {
        this.oidcUsers.add(oidcuser);
    }
    
    public void removeOidcUser(OidcUser oidcuser) {
        this.oidcUsers.remove(oidcuser);
    }
}

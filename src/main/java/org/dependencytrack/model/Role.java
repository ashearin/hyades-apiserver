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

import alpine.common.validation.RegexSequence;
import alpine.model.LdapUser;
import alpine.model.ManagedUser;
import alpine.model.OidcUser;
import alpine.server.json.TrimmedStringDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.Order;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import java.io.Serializable;
import java.security.Permission;
import java.util.List;
import java.util.Map;

/**
 * Model for tracking roles. Roles map a set of permissions
 * to a user within the scope of a Role.
 *
 * @author Allen Shearin
 * @since 5.6.0
 */
@PersistenceCapable
@FetchGroups({
        @FetchGroup(name = "ALL", members = {
                @Persistent(name = "name"),
                @Persistent(name = "description"),
                @Persistent(name = "permissions"),
                @Persistent(name = "projects"),
                @Persistent(name = "oidcUserProjectPermissions"),
                @Persistent(name = "ldapUserProjectPermissions"),
                @Persistent(name = "managedUserProjectPermissions")
        }),
        @FetchGroup(name = "OIDC_ACCESS", members = {
            @Persistent(name = "name"),
            @Persistent(name = "permissions"),
            @Persistent(name = "projects"),
            @Persistent(name = "oidcUserProjectPermissions"),
        }),
        @FetchGroup(name = "LDAP_ACCESS", members = {
            @Persistent(name = "name"),
            @Persistent(name = "permissions"),
            @Persistent(name = "projects"),
            @Persistent(name = "ldapUserProjectPermissions"),
        }),
        @FetchGroup(name = "MANAGED_ACCESS", members = {
            @Persistent(name = "name"),
            @Persistent(name = "permissions"),
            @Persistent(name = "projects"),
            @Persistent(name = "managedUserProjectPermissions"),
        }),
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Role implements Serializable {

    private static final long serialVersionUID = -7592438796591673355L;

    /**
     * Defines JDO fetch groups for this class.
     */
    public enum FetchGroup {
        ALL,
        OIDC_ACCESS,
        LDAP_ACCESS,
        MANAGED_ACCESS
    }

    record OidcUserProject(OidcUser oidcUser, Project project) {
    }

    record LdapUserProject(LdapUser ldapUser, Project project) {
    }

    record ManagedUserProject(ManagedUser managedUser, Project project) {
    }

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.NATIVE)
    @JsonIgnore
    private long id;

    @Persistent
    @Index(name = "ROLE_NAME_IDX")
    @Column(name = "NAME", jdbcType = "VARCHAR", allowsNull = "false")
    @NotBlank
    @Size(min = 1, max = 255)
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    @Pattern(regexp = RegexSequence.Definition.PRINTABLE_CHARS, message = "The name may only contain printable characters")
    private String name;

    @Persistent
    @Column(name = "DESCRIPTION", jdbcType = "VARCHAR")
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    @Pattern(regexp = RegexSequence.Definition.PRINTABLE_CHARS, message = "The description may only contain printable characters")
    private String description;

    @Persistent(table = "ROLES_PERMISSIONS", defaultFetchGroup = "true")
    @Index(name = "ROLES_PERMISSIONS_IDX")
    @Join(column = "ROLE_ID")
    @Element(column = "PERMISSION_ID")
    @Order(extensions = @Extension(vendorName = "datanucleus", key = "list-ordering", value = "name ASC"))
    private List<Permission> permissions;

    @Persistent(defaultFetchGroup = "true")
    @Element(column = "PROJECT_ID")
    @Order(extensions = @Extension(vendorName = "datanucleus", key = "list-ordering", value = "name ASC"))
    private List<Project> projects;

    @Persistent(table = "OIDCUSERS_PROJECTS_ROLES")
    private Map<OidcUserProject, List<Permission>> oidcUserProjectPermissions;

    @Persistent(table = "LDAPUSERS_PROJECTS_ROLES")
    private Map<LdapUserProject, List<Permission>> ldapUserProjectPermissions;

    @Persistent(table = "MANAGEDUSERS_PROJECTS_ROLES")
    private Map<ManagedUserProject, List<Permission>> managedUserProjectPermissions;

    // @Persistent(mappedBy = "teams")
    // @Order(extensions = @Extension(vendorName = "datanucleus", key =
    // "list-ordering", value = "username ASC"))
    // private List<OidcUser> oidcUsers;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        if (getDescription() != null) {
            sb.append(" : ").append(getDescription());
        }
        return sb.toString();
    }
}

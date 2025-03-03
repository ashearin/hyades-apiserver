package org.dependencytrack.persistence;

import java.nio.file.attribute.UserPrincipal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.dependencytrack.model.Role;

import alpine.common.logging.Logger;
import alpine.model.Permission;
import alpine.resources.AlpineRequest;

final class RoleQueryManager extends QueryManager implements IQueryManager {

    private static final Logger LOGGER = Logger.getLogger(ProjectQueryManager.class);

    RoleQueryManager(final PersistenceManager pm) {
        super(pm);
    }

    RoleQueryManager(final PersistenceManager pm, final AlpineRequest request) {
        super(pm, request);
    }

    boolean addRoleToUser(UserPrincipal principal, Role role, String roleName, String projectName){
        //WARNING: This method is a stub.
        //TODO: Implement addRoleToUser
        return true;
    }

    boolean removeRoleFromUser(UserPrincipal principal, Role role, String roleName, String projectName){
        //WARNING: This method is a stub.
        //TODO: Implement removeRoleFromUser
        return true;
    }

    public List<Role> getRoles() {
        final Query<Role> query = pm.newQuery(Role.class);
        if (orderBy == null)
            query.setOrdering("name asc");

        return query.executeList();
    }

    public List<Permission> getUserProjectPermissions(final String userName, final String projectName) {
            final Map.Entry<String, Map<String, Object>> projectAclConditionAndParams = getProjectAclSqlCondition();
            final String projectAclCondition = projectAclConditionAndParams.getKey();
            final Map<String, Object> projectAclConditionParams = projectAclConditionAndParams.getValue();
    
            // language=SQL
            var sqlQuery = """
                    SELECT
                        "PERMISSION"."NAME",
                        "PERMISSION"."DESCRIPTION"
                    FROM "PERMISSION"
                    INNER JOIN "ROLES_PERMISSIONS"
                        ON "PERMISSION"."ID" = "ROLES_PERMISSIONS"."PERMISSION_ID"
                    INNER JOIN "ROLE"
                        ON "ROLE"."ID" = "ROLES_PERMISSIONS"."ROLE_ID"
                    INNER JOIN "PROJECT_ACCESS_ROLES"
                    
                        ON "PROJECT_ACCESS_ROLES"."ROLE_ID" = "ROLE"."ID"
                    INNER JOIN "PROJECT"
                        ON "PROJECT"."ID" = "PROJECT_ACCESS_ROLES"."PROJECT_ID"
                    INNER JOIN "MANAGEDUSERS_PROJECTS_ROLES"
                        ON "MANAGEDUSERS_PROJECTS_ROLES"."PROJECT_ACCESS_ROLE_ID" = "PROJECT_ACCESS_ROLES"."ID"
                    INNER JOIN "MANAGEDUSER"
                        ON "MANAGEDUSER"."ID" = "MANAGEDUSERS_PROJECTS_ROLES"."MANAGEDUSER_ID"
                    WHERE
                        "MANAGEDUSER"."USERNAME" = :userName AND
                        "PROJECT"."NAME" = :projectName
                    """.formatted(projectAclCondition);
    
            final var params = new HashMap<>(projectAclConditionParams);
    
    
            sqlQuery += " " + getOffsetLimitSqlClause();
    
            final Query<?> query = pm.newQuery(Query.SQL, sqlQuery);
            query.setNamedParameters(params);
            return executeAndCloseResultList(query, Permission.class);
        }
}

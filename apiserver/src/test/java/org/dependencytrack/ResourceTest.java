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
package org.dependencytrack;

import alpine.Config;
import alpine.model.Permission;
import alpine.model.Team;
import alpine.server.auth.PasswordService;
import alpine.server.persistence.PersistenceManagerFactory;
import org.apache.kafka.clients.producer.MockProducer;
import org.dependencytrack.auth.Permissions;
import org.dependencytrack.event.kafka.KafkaProducerInitializer;
import org.dependencytrack.model.ConfigPropertyConstants;
import org.dependencytrack.persistence.QueryManager;
import org.dependencytrack.plugin.PluginManagerTestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.ws.rs.core.Response;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.dependencytrack.PersistenceCapableTest.configurePmf;
import static org.dependencytrack.PersistenceCapableTest.truncateTables;

public abstract class ResourceTest {

    protected final String V1_ACL = "/v1/acl";
    protected final String V1_ANALYSIS = "/v1/analysis";
    protected final String V1_BADGE = "/v1/badge";
    protected final String V1_BOM = "/v1/bom";
    protected final String V1_CALCULATOR = "/v1/calculator";
    protected final String V1_COMPONENT = "/v1/component";
    protected final String V1_DEPENDENCY_GRAPH = "/v1/dependencyGraph";
    protected final String V1_CONFIG_PROPERTY = "/v1/configProperty";
    protected final String V1_CWE = "/v1/cwe";
    protected final String V1_DEPENDENCY = "/v1/dependency";
    protected final String V1_EVENT = "/v1/event";
    protected final String V1_FINDING = "/v1/finding";
    protected final String V1_LDAP = "/v1/ldap";
    protected final String V1_LICENSE = "/v1/license";
    protected final String V1_METRICS = "/v1/metrics";
    protected final String V1_NOTIFICATION_PUBLISHER = "/v1/notification/publisher";
    protected final String V1_NOTIFICATION_RULE = "/v1/notification/rule";
    protected final String V1_OIDC = "/v1/oidc";
    protected final String V1_PERMISSION = "/v1/permission";
    protected final String V1_OSV_ECOSYSTEM = "/v1/integration/osv/ecosystem";
    protected final String V1_POLICY = "/v1/policy";
    protected final String V1_POLICY_VIOLATION = "/v1/violation";
    protected final String V1_PROJECT = "/v1/project";
    protected final String V1_PROJECT_LATEST = "/v1/project/latest/";
    protected final String V1_REPOSITORY = "/v1/repository";
    protected final String V1_ROLE = "/v1/role";
    protected final String V1_SCAN = "/v1/scan";
    protected final String V1_SEARCH = "/v1/search";
    protected final String V1_TEAM = "/v1/team";
    protected final String V1_USER = "/v1/user";
    protected final String V1_VEX = "/v1/vex";
    protected final String V1_VIOLATION_ANALYSIS = "/v1/violation/analysis";
    protected final String V1_VULNERABILITY = "/v1/vulnerability";
    protected final String V1_VULNERABILITY_POLICY = "/v1/policy/vulnerability";
    protected final String V1_VULNERABILITY_POLICY_BUNDLE = "/v1/policy/vulnerability/bundle";
    protected final String V1_WORKFLOW = "/v1/workflow";
    protected final String ORDER_BY = "orderBy";
    protected final String SORT = "sort";
    protected final String SORT_ASC = "asc";
    protected final String SORT_DESC = "desc";
    protected final String FILTER = "filter";
    protected final String PAGE = "page";
    protected final String SIZE = "size";
    protected final String TOTAL_COUNT_HEADER = "X-Total-Count";
    protected final String X_API_KEY = "X-Api-Key";
    protected final String API_KEY = "apiKey";
    protected final String V1_TAG = "/v1/tag";

    // Hashing is expensive. Do it once and re-use across tests as much as possible.
    protected static final String TEST_USER_PASSWORD_HASH = new String(PasswordService.createHash("testuser".toCharArray()));

    protected static PostgresTestContainer postgresContainer;

    protected QueryManager qm;
    protected MockProducer<byte[], byte[]> kafkaMockProducer;
    protected Team team;
    protected String apiKey;

    @BeforeClass
    public static void init() {
        Config.enableUnitTests();

        postgresContainer = new PostgresTestContainer();
        postgresContainer.start();
    }

    @Before
    public void before() throws Exception {
        truncateTables(postgresContainer);
        configurePmf(postgresContainer);

        // Add a test user and team with API key. Optional if this is used, but its available to all tests.
        this.qm = new QueryManager();
        PluginManagerTestUtil.loadPlugins();
        this.kafkaMockProducer = (MockProducer<byte[], byte[]>) KafkaProducerInitializer.getProducer();
        team = qm.createTeam("Test Users");
        this.apiKey = qm.createApiKey(team).getKey();
    }

    @After
    public void after() {
        PluginManagerTestUtil.unloadPlugins();

        // PersistenceManager will refuse to close when there's an active transaction
        // that was neither committed nor rolled back. Unfortunately some areas of the
        // code base can leave such a broken state behind if they run into unexpected
        // errors. See: https://github.com/DependencyTrack/dependency-track/issues/2677
        if (!qm.getPersistenceManager().isClosed()
            && qm.getPersistenceManager().currentTransaction().isActive()) {
            qm.getPersistenceManager().currentTransaction().rollback();
        }

        PersistenceManagerFactory.tearDown();
        KafkaProducerInitializer.tearDown();
    }

    @AfterClass
    public static void tearDownClass() {
        if (postgresContainer != null) {
            postgresContainer.stopWhenNotReusing();
        }
    }

    public void initializeWithPermissions(Permissions... permissions) {
        List<Permission> permissionList = new ArrayList<>();
        for (Permissions permission : permissions) {
            permissionList.add(qm.createPermission(permission.name(), null));
        }
        team.setPermissions(permissionList);
        qm.persist(team);
    }

    protected void enablePortfolioAccessControl() {
        qm.createConfigProperty(
                ConfigPropertyConstants.ACCESS_MANAGEMENT_ACL_ENABLED.getGroupName(),
                ConfigPropertyConstants.ACCESS_MANAGEMENT_ACL_ENABLED.getPropertyName(),
                "true",
                ConfigPropertyConstants.ACCESS_MANAGEMENT_ACL_ENABLED.getPropertyType(),
                null
        );
    }

    protected String getPlainTextBody(Response response) {
        return response.readEntity(String.class);
    }

    protected JsonObject parseJsonObject(Response response) {
        StringReader stringReader = new StringReader(response.readEntity(String.class));
        try (JsonReader jsonReader = Json.createReader(stringReader)) {
            return jsonReader.readObject();
        }
    }

    protected JsonArray parseJsonArray(Response response) {
        StringReader stringReader = new StringReader(response.readEntity(String.class));
        try (JsonReader jsonReader = Json.createReader(stringReader)) {
            return jsonReader.readArray();
        }
    }
}

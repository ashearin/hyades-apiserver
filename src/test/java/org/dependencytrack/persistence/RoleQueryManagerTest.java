package org.dependencytrack.persistence;

import alpine.Config;
import alpine.model.Permission;

import java.util.List;

import org.dependencytrack.PersistenceCapableTest;
import org.dependencytrack.event.kafka.KafkaProducerInitializer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class RoleQueryManagerTest extends PersistenceCapableTest {

    @BeforeClass
    public static void beforeClass() {
        Config.enableUnitTests();
    }

    @AfterClass
    public static void afterClass() {
        KafkaProducerInitializer.tearDown();
    }

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    @Test
    public void testGetUserProjectPermissions() {
        List<Permission> permissions = qm.getUserProjectPermissions("", "");
    }

}

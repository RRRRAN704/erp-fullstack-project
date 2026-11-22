package com.erp;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Paths;

/**
 * Base class for integration tests: the whole Spring app against a real MySQL (seeded with erp_project.sql)
 * and a real Redis, both started once in Docker and shared by every test class.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("erp_project")
            // Same seed file production starts from; MySQL runs it once on first start.
            .withCopyFileToContainer(
                    MountableFile.forHostPath(Paths.get("..", "erp_project.sql")),
                    "/docker-entrypoint-initdb.d/01-erp_project.sql");

    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    // Started once per JVM (not per class) so all tests reuse the same containers; Testcontainers removes them at exit.
    static {
        MYSQL.start();
        REDIS.start();
    }

    // Fills the ${erp.*} placeholders in application.yml with the containers' random ports.
    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        registry.add("erp.datasource.host", MYSQL::getHost);
        registry.add("erp.datasource.port", () -> MYSQL.getMappedPort(3306));
        registry.add("erp.datasource.database", MYSQL::getDatabaseName);
        registry.add("erp.datasource.username", MYSQL::getUsername);
        registry.add("erp.datasource.password", MYSQL::getPassword);
        registry.add("erp.redis.host", REDIS::getHost);
        registry.add("erp.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("erp.redis.password", () -> "");
    }
}

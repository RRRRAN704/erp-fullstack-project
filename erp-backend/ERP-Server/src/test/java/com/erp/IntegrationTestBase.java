package com.erp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Paths;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    /** Password of every seeded employee (admin, manager, yrwang, adam). */
    protected static final String SEED_PASSWORD = "123456";

    @Autowired
    protected MockMvc mockMvc;

    private static final ObjectMapper JSON = new ObjectMapper();

    /** Seed employees used by the business tests (see erp_project.sql). */
    protected static final long YRWANG_ID = 2;     // Consultant, department 2
    protected static final long DEPARTMENT_2 = 2;

    /** Serialises a request body (e.g. a Map) to JSON. */
    protected static String json(Object body) throws Exception {
        return JSON.writeValueAsString(body);
    }

    /** A name no other test (or earlier run against the same data) uses, so searches find exactly one row. */
    protected static String unique(String prefix) {
        return prefix + " " + java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    /** Logs in through the real endpoint and returns the JWT to send in the "token" header. */
    protected String loginAs(String username) throws Exception {
        String body = mockMvc.perform(post("/employee/info/login")
                        .contentType(APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + SEED_PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.data.token");
    }
}

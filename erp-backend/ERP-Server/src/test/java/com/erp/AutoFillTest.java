package com.erp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * AutoFillAspect (AOP) fills create/update time and user on mapper methods marked @AutoFill.
 * The columns are nullable, so if the aspect silently stopped running (e.g. AOP not enabled
 * after a Spring Boot upgrade) nothing else would fail: check the values in the database.
 */
class AutoFillTest extends IntegrationTestBase {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void insertAndUpdateFillAuditColumns() throws Exception {
        String token = loginAs("yrwang");
        String name = unique("AutoFill Client");

        Map<String, Object> client = new HashMap<>();
        client.put("employeeId", YRWANG_ID);
        client.put("departmentId", DEPARTMENT_2);
        client.put("companyName", name);
        client.put("contactPerson", "Test Person");
        client.put("contactPhone", "07000000000");
        client.put("contactEmail", "client@example.com");
        client.put("address", "1 Test Street, London");
        client.put("isArchived", 0);
        mockMvc.perform(post("/employee/client").header("token", token)
                        .contentType(APPLICATION_JSON).content(json(client)))
                .andExpect(jsonPath("$.code").value(1));

        // INSERT: all four columns filled, user = the logged-in employee
        Map<String, Object> row = auditColumns(name);
        assertThat(row.get("create_time")).isNotNull();
        assertThat(row.get("update_time")).isNotNull();
        assertThat(((Number) row.get("create_user")).longValue()).isEqualTo(YRWANG_ID);
        assertThat(((Number) row.get("update_user")).longValue()).isEqualTo(YRWANG_ID);

        // UPDATE by another user (the department manager): update_user changes, create_user stays
        long managerId = jdbcTemplate.queryForObject(
                "select id from employee where username = 'manager'", Long.class);
        Map<String, Object> edit = new HashMap<>();
        edit.put("id", ((Number) row.get("id")).longValue());
        edit.put("contactPerson", "Edited Person");
        mockMvc.perform(put("/employee/client").header("token", loginAs("manager"))
                        .contentType(APPLICATION_JSON).content(json(edit)))
                .andExpect(jsonPath("$.code").value(1));

        Map<String, Object> updated = auditColumns(name);
        assertThat(((Number) updated.get("create_user")).longValue()).isEqualTo(YRWANG_ID);
        assertThat(((Number) updated.get("update_user")).longValue()).isEqualTo(managerId);
    }

    private Map<String, Object> auditColumns(String companyName) {
        return jdbcTemplate.queryForMap(
                "select id, create_time, update_time, create_user, update_user from client where company_name = ?",
                companyName);
    }
}

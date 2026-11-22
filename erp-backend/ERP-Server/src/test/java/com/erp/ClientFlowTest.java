package com.erp;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/** Clients: create, search, edit, archive and restore, and who can see them. */
class ClientFlowTest extends IntegrationTestBase {

    @Test
    void consultantCreatesEditsArchivesAndRestoresClient() throws Exception {
        String token = loginAs("yrwang");
        String name = unique("Test Client");

        mockMvc.perform(post("/employee/client").header("token", token)
                        .contentType(APPLICATION_JSON).content(json(newClient(name))))
                .andExpect(jsonPath("$.code").value(1));

        // Found by name in the active list
        long id = ((Number) JsonPath.read(
                activePage(token, name).andExpect(jsonPath("$.data.total").value(1))
                        .andReturn().getResponse().getContentAsString(),
                "$.data.records[0].id")).longValue();

        // Edit
        Map<String, Object> edit = new HashMap<>();
        edit.put("id", id);
        edit.put("contactPerson", "Edited Person");
        mockMvc.perform(put("/employee/client").header("token", token)
                        .contentType(APPLICATION_JSON).content(json(edit)))
                .andExpect(jsonPath("$.code").value(1));
        activePage(token, name).andExpect(jsonPath("$.data.records[0].contactPerson").value("Edited Person"));

        // Archive: leaves the active list, appears in the archived list
        mockMvc.perform(put("/employee/client/archive").header("token", token)
                        .contentType(APPLICATION_JSON).content(json(List.of(id))))
                .andExpect(jsonPath("$.code").value(1));
        activePage(token, name).andExpect(jsonPath("$.data.total").value(0));
        archivedPage(token, name).andExpect(jsonPath("$.data.total").value(1));

        // Restore: back in the active list
        mockMvc.perform(put("/employee/client/restore").header("token", token)
                        .contentType(APPLICATION_JSON).content(json(List.of(id))))
                .andExpect(jsonPath("$.code").value(1));
        activePage(token, name).andExpect(jsonPath("$.data.total").value(1));
        archivedPage(token, name).andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void managerSeesDepartmentClientsButOtherConsultantDoesNot() throws Exception {
        String name = unique("Visibility Client");
        mockMvc.perform(post("/employee/client").header("token", loginAs("yrwang"))
                        .contentType(APPLICATION_JSON).content(json(newClient(name))))
                .andExpect(jsonPath("$.code").value(1));

        // Manager of department 2 sees every client in the department
        activePage(loginAs("manager"), name).andExpect(jsonPath("$.data.total").value(1));
        // Consultants see only their own clients
        activePage(loginAs("adam"), name).andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void adminCannotUseClientEndpoints() throws Exception {
        activePage(loginAs("admin"), "anything")
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("Admin does not have access to this resource"));
    }

    private Map<String, Object> newClient(String companyName) {
        // Same fields the ERP frontend sends
        Map<String, Object> client = new HashMap<>();
        client.put("employeeId", YRWANG_ID);
        client.put("departmentId", DEPARTMENT_2);
        client.put("companyName", companyName);
        client.put("contactPerson", "Test Person");
        client.put("contactPhone", "07000000000");
        client.put("contactEmail", "client@example.com");
        client.put("address", "1 Test Street, London");
        client.put("isArchived", 0);
        return client;
    }

    private ResultActions activePage(String token, String name) throws Exception {
        return mockMvc.perform(get("/employee/client/page").header("token", token)
                .param("page", "1").param("pageSize", "10").param("companyName", name));
    }

    private ResultActions archivedPage(String token, String name) throws Exception {
        return mockMvc.perform(get("/employee/client/page/archived").header("token", token)
                .param("page", "1").param("pageSize", "10").param("companyName", name));
    }
}

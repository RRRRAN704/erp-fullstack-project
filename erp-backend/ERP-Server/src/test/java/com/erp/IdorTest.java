package com.erp;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * IDOR (Insecure Direct Object Reference): update/archive/restore take a record id from the request,
 * so a user could change any record by guessing ids. Rule: you may only change what you can see in your list,
 * and only assign records to people you are allowed to (consultant: yourself; manager: your department).
 *
 * Seed org: department 1 = admin (id 1); department 2 = yrwang (id 2, consultant), manager (id 3), adam (id 6, consultant).
 */
class IdorTest extends IntegrationTestBase {

    private static final long ADMIN_ID = 1;      // department 1
    private static final long ADAM_ID = 6;       // consultant, department 2
    private static final String NO_PERMISSION = "No permission to modify this record";
    private static final String CANNOT_ASSIGN = "No permission to assign to this employee";

    // ---------- clients ----------

    @Test
    void consultantCannotEditArchiveOrRestoreAnotherConsultantsClient() throws Exception {
        String name = unique("Idor Client");
        long id = createClient(loginAs("yrwang"), name, YRWANG_ID);
        String adam = loginAs("adam");

        Map<String, Object> edit = new HashMap<>();
        edit.put("id", id);
        edit.put("companyName", "Hijacked");
        expectError(put("/employee/client", adam, edit), NO_PERMISSION);
        expectError(put("/employee/client/archive", adam, List.of(id)), NO_PERMISSION);
        expectError(put("/employee/client/restore", adam, List.of(id)), NO_PERMISSION);

        // Nothing changed: still active, still the original name
        clientPage(loginAs("yrwang"), name, false).andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void batchIsRejectedAsAWholeIfOneIdIsNotYours() throws Exception {
        String own = unique("Idor Own");
        String other = unique("Idor Other");
        String adam = loginAs("adam");
        long ownId = createClient(adam, own, ADAM_ID);
        long otherId = createClient(loginAs("yrwang"), other, YRWANG_ID);

        expectError(put("/employee/client/archive", adam, List.of(ownId, otherId)), NO_PERMISSION);

        // Not even adam's own client was archived
        clientPage(adam, own, false).andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void consultantCanOnlyAssignClientsToThemselves() throws Exception {
        String adam = loginAs("adam");

        // Create a client in someone else's name
        expectError(post("/employee/client", adam, newClient(unique("Idor Planted"), YRWANG_ID)), CANNOT_ASSIGN);

        // Hand an own client over to someone else
        long id = createClient(adam, unique("Idor Handover"), ADAM_ID);
        Map<String, Object> edit = new HashMap<>();
        edit.put("id", id);
        edit.put("employeeId", YRWANG_ID);
        expectError(put("/employee/client", adam, edit), CANNOT_ASSIGN);
    }

    @Test
    void departmentComesFromTheEmployeeNotTheRequest() throws Exception {
        String name = unique("Idor Dept");
        Map<String, Object> client = newClient(name, YRWANG_ID);
        client.put("departmentId", 1);   // lie about the department
        expect(post("/employee/client", loginAs("yrwang"), client)).andExpect(jsonPath("$.code").value(1));

        // Stored with yrwang's real department (2), so their manager sees it
        clientPage(loginAs("manager"), name, false)
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].departmentId").value(DEPARTMENT_2));
    }

    @Test
    void managerWorksWithinOwnDepartmentOnly() throws Exception {
        String manager = loginAs("manager");
        String name = unique("Idor Manager");
        long id = createClient(loginAs("yrwang"), name, YRWANG_ID);

        // Allowed: reassign a department client to another consultant in the department
        Map<String, Object> edit = new HashMap<>();
        edit.put("id", id);
        edit.put("employeeId", ADAM_ID);
        expect(put("/employee/client", manager, edit)).andExpect(jsonPath("$.code").value(1));
        clientPage(loginAs("adam"), name, false).andExpect(jsonPath("$.data.total").value(1));

        // Not allowed: move it to an employee of another department
        edit.put("employeeId", ADMIN_ID);
        expectError(put("/employee/client", manager, edit), CANNOT_ASSIGN);
        expectError(post("/employee/client", manager, newClient(unique("Idor Other Dept"), ADMIN_ID)), CANNOT_ASSIGN);
    }

    @Test
    void unknownIdIsRejected() throws Exception {
        expectError(put("/employee/client/archive", loginAs("manager"), List.of(999_999L)), NO_PERMISSION);
    }

    // ---------- contracts ----------

    @Test
    void consultantCannotEditArchiveOrRestoreAnotherConsultantsContract() throws Exception {
        String yrwang = loginAs("yrwang");
        long clientId = createClient(yrwang, unique("Idor Contract Client"), YRWANG_ID);
        String name = unique("Idor Contract");
        long id = createContract(yrwang, name, YRWANG_ID, clientId);
        String adam = loginAs("adam");

        Map<String, Object> edit = new HashMap<>();
        edit.put("id", id);
        edit.put("amount", 1);
        expectError(put("/employee/contract", adam, edit), NO_PERMISSION);
        expectError(put("/employee/contract/archive", adam, List.of(id)), NO_PERMISSION);
        expectError(put("/employee/contract/restore", adam, List.of(id)), NO_PERMISSION);

        contractPage(yrwang, name).andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void contractCanOnlyUseAClientYouCanSee() throws Exception {
        long yrwangsClient = createClient(loginAs("yrwang"), unique("Idor Foreign Client"), YRWANG_ID);
        String adam = loginAs("adam");

        // New contract on someone else's client
        expectError(post("/employee/contract", adam, newContract(unique("Idor Contract"), ADAM_ID, yrwangsClient)),
                NO_PERMISSION);

        // Move an own contract onto someone else's client
        long ownClient = createClient(adam, unique("Idor Own Client"), ADAM_ID);
        long id = createContract(adam, unique("Idor Contract"), ADAM_ID, ownClient);
        Map<String, Object> edit = new HashMap<>();
        edit.put("id", id);
        edit.put("clientId", yrwangsClient);
        expectError(put("/employee/contract", adam, edit), NO_PERMISSION);
    }

    @Test
    void contractAssigneeRulesMatchClients() throws Exception {
        String adam = loginAs("adam");
        long ownClient = createClient(adam, unique("Idor Assign Client"), ADAM_ID);
        expectError(post("/employee/contract", adam, newContract(unique("Idor Contract"), YRWANG_ID, ownClient)),
                CANNOT_ASSIGN);

        String manager = loginAs("manager");
        String name = unique("Idor Contract");
        long id = createContract(adam, name, ADAM_ID, ownClient);
        Map<String, Object> edit = new HashMap<>();
        edit.put("id", id);
        edit.put("employeeId", ADMIN_ID);
        expectError(put("/employee/contract", manager, edit), CANNOT_ASSIGN);
        edit.put("employeeId", YRWANG_ID);   // within the department: fine
        expect(put("/employee/contract", manager, edit)).andExpect(jsonPath("$.code").value(1));
        contractPage(manager, name).andExpect(jsonPath("$.data.records[0].departmentId").value(DEPARTMENT_2));
    }

    // ---------- helpers ----------

    private long createClient(String token, String name, long employeeId) throws Exception {
        expect(post("/employee/client", token, newClient(name, employeeId))).andExpect(jsonPath("$.code").value(1));
        String body = clientPage(token, name, false).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.data.records[0].id")).longValue();
    }

    private Map<String, Object> newClient(String companyName, long employeeId) {
        Map<String, Object> client = new HashMap<>();
        client.put("employeeId", employeeId);
        client.put("departmentId", DEPARTMENT_2);
        client.put("companyName", companyName);
        client.put("contactPerson", "Test Person");
        client.put("contactPhone", "07000000000");
        client.put("contactEmail", "client@example.com");
        client.put("address", "1 Test Street, London");
        client.put("isArchived", 0);
        return client;
    }

    private ResultActions clientPage(String token, String name, boolean archived) throws Exception {
        String url = archived ? "/employee/client/page/archived" : "/employee/client/page";
        return mockMvc.perform(get(url).header("token", token)
                .param("page", "1").param("pageSize", "10").param("companyName", name));
    }

    private long createContract(String token, String name, long employeeId, long clientId) throws Exception {
        expect(post("/employee/contract", token, newContract(name, employeeId, clientId)))
                .andExpect(jsonPath("$.code").value(1));
        String body = contractPage(token, name).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.data.records[0].id")).longValue();
    }

    private Map<String, Object> newContract(String contractName, long employeeId, long clientId) {
        Map<String, Object> contract = new HashMap<>();
        contract.put("contractName", contractName);
        contract.put("contractNumber", "C-" + contractName.substring(contractName.length() - 8));
        contract.put("employeeId", employeeId);
        contract.put("clientId", clientId);
        contract.put("departmentId", DEPARTMENT_2);
        contract.put("amount", 1000);
        contract.put("startDate", "2026-01-01");
        contract.put("endDate", "2026-12-31");
        contract.put("isArchived", 0);
        return contract;
    }

    private ResultActions contractPage(String token, String name) throws Exception {
        return mockMvc.perform(get("/employee/contract/page").header("token", token)
                .param("page", "1").param("pageSize", "10").param("contractName", name));
    }

    private RequestBuilder put(String url, String token, Object body) throws Exception {
        return MockMvcRequestBuilders.put(url).header("token", token)
                .contentType(APPLICATION_JSON).content(json(body));
    }

    private RequestBuilder post(String url, String token, Object body) throws Exception {
        return MockMvcRequestBuilders.post(url).header("token", token)
                .contentType(APPLICATION_JSON).content(json(body));
    }

    private ResultActions expect(RequestBuilder request) throws Exception {
        return mockMvc.perform(request);
    }

    private void expectError(RequestBuilder request, String message) throws Exception {
        mockMvc.perform(request)
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value(message));
    }
}

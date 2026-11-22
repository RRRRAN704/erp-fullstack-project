package com.erp;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/** Contracts: create, search, edit, archive and restore, visibility, and the dashboard reports that count them. */
class ContractFlowTest extends IntegrationTestBase {

    private static final long SEED_CLIENT_OF_YRWANG = 1;  // 'TechNova Ltd' in erp_project.sql

    @Test
    void consultantCreatesEditsArchivesAndRestoresContract() throws Exception {
        String token = loginAs("yrwang");
        String name = unique("Test Contract");

        mockMvc.perform(post("/employee/contract").header("token", token)
                        .contentType(APPLICATION_JSON).content(json(newContract(name, 5000))))
                .andExpect(jsonPath("$.code").value(1));

        String page = activePage(token, name)
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].amount").value(5000))
                .andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(page, "$.data.records[0].id")).longValue();

        Map<String, Object> edit = new HashMap<>();
        edit.put("id", id);
        edit.put("amount", 7500);
        mockMvc.perform(put("/employee/contract").header("token", token)
                        .contentType(APPLICATION_JSON).content(json(edit)))
                .andExpect(jsonPath("$.code").value(1));
        activePage(token, name).andExpect(jsonPath("$.data.records[0].amount").value(7500));

        mockMvc.perform(put("/employee/contract/archive").header("token", token)
                        .contentType(APPLICATION_JSON).content(json(List.of(id))))
                .andExpect(jsonPath("$.code").value(1));
        activePage(token, name).andExpect(jsonPath("$.data.total").value(0));
        archivedPage(token, name).andExpect(jsonPath("$.data.total").value(1));

        mockMvc.perform(put("/employee/contract/restore").header("token", token)
                        .contentType(APPLICATION_JSON).content(json(List.of(id))))
                .andExpect(jsonPath("$.code").value(1));
        activePage(token, name).andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void managerSeesDepartmentContractsButOtherConsultantDoesNot() throws Exception {
        String name = unique("Visibility Contract");
        mockMvc.perform(post("/employee/contract").header("token", loginAs("yrwang"))
                        .contentType(APPLICATION_JSON).content(json(newContract(name, 1000))))
                .andExpect(jsonPath("$.code").value(1));

        activePage(loginAs("manager"), name).andExpect(jsonPath("$.data.total").value(1));
        activePage(loginAs("adam"), name).andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void reportsListEveryDayInRangeAndCountTodaysContract() throws Exception {
        String token = loginAs("manager");
        LocalDate today = LocalDate.now();
        String begin = today.minusDays(2).toString();
        String end = today.toString();

        mockMvc.perform(post("/employee/contract").header("token", loginAs("yrwang"))
                        .contentType(APPLICATION_JSON).content(json(newContract(unique("Report Contract"), 1234))))
                .andExpect(jsonPath("$.code").value(1));

        // Three days requested -> three entries, the last one being today
        mockMvc.perform(get("/employee/report/contractNumber").header("token", token)
                        .param("begin", begin).param("end", end))
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.dateList").value(String.join(",",
                        today.minusDays(2).toString(), today.minusDays(1).toString(), end)));

        String amounts = mockMvc.perform(get("/employee/report/contractAmount").header("token", token)
                        .param("begin", end).param("end", end))
                .andExpect(jsonPath("$.code").value(1))
                .andReturn().getResponse().getContentAsString();
        int todaysTotal = Integer.parseInt(JsonPath.read(amounts, "$.data.contractAmountList"));
        assertThat(todaysTotal).isGreaterThanOrEqualTo(1234);

        mockMvc.perform(get("/employee/report/topEmployee").header("token", token)
                        .param("begin", begin).param("end", end))
                .andExpect(jsonPath("$.code").value(1));
        mockMvc.perform(get("/employee/report/orderNumber").header("token", token)
                        .param("begin", begin).param("end", end))
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.dateList", endsWith(end)));
    }

    private Map<String, Object> newContract(String contractName, int amount) {
        // Same fields the ERP frontend sends
        Map<String, Object> contract = new HashMap<>();
        contract.put("contractName", contractName);
        contract.put("contractNumber", "C-" + contractName.substring(contractName.length() - 8));
        contract.put("employeeId", YRWANG_ID);
        contract.put("clientId", SEED_CLIENT_OF_YRWANG);
        contract.put("departmentId", DEPARTMENT_2);
        contract.put("amount", amount);
        contract.put("startDate", "2026-01-01");
        contract.put("endDate", "2026-12-31");
        contract.put("isArchived", 0);
        return contract;
    }

    private ResultActions activePage(String token, String name) throws Exception {
        return mockMvc.perform(get("/employee/contract/page").header("token", token)
                .param("page", "1").param("pageSize", "10").param("contractName", name));
    }

    private ResultActions archivedPage(String token, String name) throws Exception {
        return mockMvc.perform(get("/employee/contract/page/archived").header("token", token)
                .param("page", "1").param("pageSize", "10").param("contractName", name));
    }
}

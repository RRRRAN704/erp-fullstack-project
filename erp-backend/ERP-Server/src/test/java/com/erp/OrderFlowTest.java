package com.erp;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * The website -> ERP flow: a visitor submits a consultation order on the public site (no login),
 * a manager sees it and assigns it to a consultant, who then sees it in their own list.
 */
class OrderFlowTest extends IntegrationTestBase {

    @Test
    void websiteOrderIsAssignedByManagerToConsultant() throws Exception {
        String company = unique("Website Lead");

        // Public website: no token needed for this one endpoint
        Map<String, Object> order = new HashMap<>();
        order.put("companyName", company);
        order.put("contactPerson", "Visitor");
        order.put("contactPhone", "07000000001");
        order.put("contactEmail", "visitor@example.com");
        order.put("remark", "Please call me back");
        mockMvc.perform(post("/employee/order").contentType(APPLICATION_JSON).content(json(order)))
                .andExpect(jsonPath("$.code").value(1));

        String managerToken = loginAs("manager");
        String consultantToken = loginAs("yrwang");

        // The manager sees the new lead; the consultant doesn't yet
        String page = orderPage(managerToken, company)
                .andExpect(jsonPath("$.data.total").value(1))
                .andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(page, "$.data.records[0].id")).longValue();
        orderPage(consultantToken, company).andExpect(jsonPath("$.data.total").value(0));

        // Only a manager may assign
        mockMvc.perform(put("/employee/order/assign").header("token", consultantToken)
                        .contentType(APPLICATION_JSON).content(json(assignment(id))))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("Only manager users can access this resource"));

        mockMvc.perform(put("/employee/order/assign").header("token", managerToken)
                        .contentType(APPLICATION_JSON).content(json(assignment(id))))
                .andExpect(jsonPath("$.code").value(1));

        // Now it shows up for the assigned consultant
        orderPage(consultantToken, company)
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].employeeId").value((int) YRWANG_ID));
    }

    private Map<String, Object> assignment(long orderId) {
        Map<String, Object> body = new HashMap<>();
        body.put("id", orderId);
        body.put("employeeId", YRWANG_ID);
        return body;
    }

    private ResultActions orderPage(String token, String company) throws Exception {
        return mockMvc.perform(get("/employee/order/page").header("token", token)
                .param("page", "1").param("pageSize", "10").param("companyName", company));
    }
}

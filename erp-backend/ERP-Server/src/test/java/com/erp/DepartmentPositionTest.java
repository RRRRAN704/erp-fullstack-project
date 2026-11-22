package com.erp;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/** Admin-only master data: departments and positions (create, search, rename, delete). */
class DepartmentPositionTest extends IntegrationTestBase {

    @Test
    void adminManagesDepartment() throws Exception {
        String token = loginAs("admin");
        String name = unique("Dept");

        Map<String, Object> dept = new HashMap<>();
        dept.put("departmentName", name);
        mockMvc.perform(post("/employee/department").header("token", token)
                        .contentType(APPLICATION_JSON).content(json(dept)))
                .andExpect(jsonPath("$.code").value(1));
        long id = idOfOnlyResult(departmentPage(token, name));

        String renamed = unique("Renamed Dept");
        dept.put("id", id);
        dept.put("departmentName", renamed);
        mockMvc.perform(put("/employee/department").header("token", token)
                        .contentType(APPLICATION_JSON).content(json(dept)))
                .andExpect(jsonPath("$.code").value(1));
        departmentPage(token, name).andExpect(jsonPath("$.data.total").value(0));
        departmentPage(token, renamed).andExpect(jsonPath("$.data.total").value(1));

        mockMvc.perform(delete("/employee/department").header("token", token).param("ids", String.valueOf(id)))
                .andExpect(jsonPath("$.code").value(1));
        departmentPage(token, renamed).andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void adminManagesPosition() throws Exception {
        String token = loginAs("admin");
        String name = unique("Position");

        Map<String, Object> position = new HashMap<>();
        position.put("positionName", name);
        position.put("departmentId", DEPARTMENT_2);
        mockMvc.perform(post("/employee/position").header("token", token)
                        .contentType(APPLICATION_JSON).content(json(position)))
                .andExpect(jsonPath("$.code").value(1));
        long id = idOfOnlyResult(positionPage(token, name));

        String renamed = unique("Renamed Position");
        position.put("id", id);
        position.put("positionName", renamed);
        mockMvc.perform(put("/employee/position").header("token", token)
                        .contentType(APPLICATION_JSON).content(json(position)))
                .andExpect(jsonPath("$.code").value(1));
        positionPage(token, renamed).andExpect(jsonPath("$.data.total").value(1));

        mockMvc.perform(delete("/employee/position").header("token", token).param("ids", String.valueOf(id)))
                .andExpect(jsonPath("$.code").value(1));
        positionPage(token, renamed).andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void managerCannotCreateDepartment() throws Exception {
        Map<String, Object> dept = new HashMap<>();
        dept.put("departmentName", unique("Dept"));
        mockMvc.perform(post("/employee/department").header("token", loginAs("manager"))
                        .contentType(APPLICATION_JSON).content(json(dept)))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("Current user is not admin"));
    }

    private long idOfOnlyResult(ResultActions page) throws Exception {
        String body = page.andExpect(jsonPath("$.data.total").value(1)).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.data.records[0].id")).longValue();
    }

    private ResultActions departmentPage(String token, String name) throws Exception {
        return mockMvc.perform(get("/employee/department/page").header("token", token)
                .param("page", "1").param("pageSize", "10").param("departmentName", name));
    }

    private ResultActions positionPage(String token, String name) throws Exception {
        return mockMvc.perform(get("/employee/position/page").header("token", token)
                .param("page", "1").param("pageSize", "10").param("positionName", name));
    }
}

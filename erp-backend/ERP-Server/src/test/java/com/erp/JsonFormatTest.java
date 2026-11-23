package com.erp;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * API responses are written with our JacksonObjectMapper (registered in WebMvcConfiguration),
 * not Spring Boot's default one: the front end expects dates as "yyyy-MM-dd HH:mm".
 */
class JsonFormatTest extends IntegrationTestBase {

    @Test
    void datesUseTheCustomFormat() throws Exception {
        String token = loginAs("admin");

        mockMvc.perform(get("/employee/info/page").header("token", token)
                        .param("page", "1").param("pageSize", "1"))
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records[0].createTime")
                        .value(matchesPattern("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")));
    }
}

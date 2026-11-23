package com.erp;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * API responses use JacksonDateTimeModule (registered in WebMvcConfiguration), not Jackson's
 * default date format: the front end expects dates as "yyyy-MM-dd HH:mm".
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

package com.erp;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Report endpoints loop over every day between begin and end (one query per day).
 * begin after end used to loop forever, and a huge range meant thousands of queries:
 * one request could tie up the server. Bad ranges must be rejected quickly.
 */
class ReportRangeTest extends IntegrationTestBase {

    @ParameterizedTest
    @ValueSource(strings = {"contractNumber", "contractAmount", "orderNumber", "topEmployee"})
    void rejectsBadDateRanges(String report) throws Exception {
        String token = loginAs("manager");
        String url = "/employee/report/" + report;

        // the timeout makes a regression fail instead of hanging the build
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            // begin after end
            expectError(get(url).header("token", token).param("begin", "2026-02-01").param("end", "2026-01-01"),
                    "Start date must not be after end date");
            // more than a year
            expectError(get(url).header("token", token).param("begin", "2000-01-01").param("end", "2026-01-01"),
                    "Date range must not exceed 366 days");
            // missing parameters (was a NullPointerException → HTTP 500)
            expectError(get(url).header("token", token),
                    "Start and end dates are required");
        });

        // a normal range still works
        mockMvc.perform(get(url).header("token", token).param("begin", "2026-01-01").param("end", "2026-01-31"))
                .andExpect(jsonPath("$.code").value(1));
    }

    private void expectError(MockHttpServletRequestBuilder request, String message) throws Exception {
        mockMvc.perform(request)
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value(message));
    }
}

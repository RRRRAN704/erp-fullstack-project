package com.erp;

import com.erp.ai.LlmClient;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * AI assistant: the LLM turns a question into a JSON command, AiRouterService parses it with
 * Jackson and returns a Jackson tree node inside the response. The LLM is replaced by a fake,
 * so this checks the parsing and the JSON output without calling a real API.
 */
class AiAskTest extends IntegrationTestBase {

    @MockitoBean
    LlmClient llmClient;

    @Test
    void questionIsParsedAndAnswered() throws Exception {
        when(llmClient.chatToJson(anyString(), anyString()))
                .thenReturn("{\"action\":\"contractNumber\",\"from\":\"2025-01-01\",\"to\":\"2026-01-01\"}");

        mockMvc.perform(get("/ai/ask").param("q", "How many contracts in 2025?"))
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.parsed.action").value("contractNumber"))
                .andExpect(jsonPath("$.data.parsed.from").value("2025-01-01"))
                .andExpect(jsonPath("$.data.parsed.to").value("2026-01-01"))
                .andExpect(jsonPath("$.data.text").isString());
    }

    @Test
    void invalidLlmOutputFallsBackToDefaults() throws Exception {
        when(llmClient.chatToJson(anyString(), anyString())).thenReturn("not json");

        mockMvc.perform(get("/ai/ask").param("q", "???"))
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.parsed.action").value("unknown"));
    }
}

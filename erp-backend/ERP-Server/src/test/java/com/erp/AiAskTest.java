package com.erp;

import com.erp.ai.LlmClient;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                .thenReturn("{\"action\":\"contractNumber\",\"from\":\"2025-01-01\",\"to\":\"2025-12-31\"}");

        mockMvc.perform(get("/ai/ask").header("token", loginAs("manager")).param("q", "How many contracts in 2025?"))
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.parsed.action").value("contractNumber"))
                .andExpect(jsonPath("$.data.parsed.from").value("2025-01-01"))
                .andExpect(jsonPath("$.data.parsed.to").value("2025-12-31"))
                .andExpect(jsonPath("$.data.text").isString());
    }

    @Test
    void promptTellsTheModelTodaysDate() throws Exception {
        // without it the model guesses "today" from its training data, so "last month" lands in the wrong year
        when(llmClient.chatToJson(anyString(), anyString())).thenReturn("{\"action\":\"unknown\"}");

        mockMvc.perform(get("/ai/ask").header("token", loginAs("manager")).param("q", "Contracts last month?"));

        ArgumentCaptor<String> systemPrompt = ArgumentCaptor.forClass(String.class);
        verify(llmClient).chatToJson(systemPrompt.capture(), eq("Contracts last month?"));
        assertThat(systemPrompt.getValue()).contains(LocalDate.now(ZoneId.of("Europe/London")).toString());
    }

    @Test
    void endDateIsIncluded() throws Exception {
        // the model returns inclusive dates; the answer and the data must cover exactly that range
        when(llmClient.chatToJson(anyString(), anyString()))
                .thenReturn("{\"action\":\"contractNumber\",\"from\":\"2026-01-01\",\"to\":\"2026-01-31\"}");

        mockMvc.perform(get("/ai/ask").header("token", loginAs("manager")).param("q", "Contracts in January 2026?"))
                .andExpect(jsonPath("$.data.text").value(org.hamcrest.Matchers.containsString("2026-01-01 to 2026-01-31")))
                .andExpect(jsonPath("$.data.data.dateList").value(org.hamcrest.Matchers.endsWith("2026-01-31")));
    }

    @Test
    void llmFailureGivesAFriendlyMessage() throws Exception {
        // e.g. out of credit (HTTP 429) or a timeout: used to surface as HTTP 500
        when(llmClient.chatToJson(anyString(), anyString())).thenThrow(new RuntimeException("LLM HTTP 429"));

        mockMvc.perform(get("/ai/ask").header("token", loginAs("manager")).param("q", "Contracts last month?"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("The AI assistant is temporarily unavailable. Please try again later."));
    }

    @Test
    void tooLongARangeIsExplainedInTheAnswer() throws Exception {
        when(llmClient.chatToJson(anyString(), anyString()))
                .thenReturn("{\"action\":\"contractAmount\",\"from\":\"2020-01-01\",\"to\":\"2025-12-31\"}");

        mockMvc.perform(get("/ai/ask").header("token", loginAs("manager")).param("q", "Contract value since 2020?"))
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.text").value(org.hamcrest.Matchers.containsString("366 days")));
    }

    @Test
    void invalidLlmOutputFallsBackToDefaults() throws Exception {
        when(llmClient.chatToJson(anyString(), anyString())).thenReturn("not json");

        mockMvc.perform(get("/ai/ask").header("token", loginAs("manager")).param("q", "???"))
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.parsed.action").value("unknown"));
    }

    @Test
    void requiresLogin() throws Exception {
        // without this, anyone could use up the LLM quota and read report data
        mockMvc.perform(get("/ai/ask").param("q", "How many contracts?"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(llmClient);
    }
}

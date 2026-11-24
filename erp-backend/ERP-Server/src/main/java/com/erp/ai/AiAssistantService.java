package com.erp.ai;

import com.erp.constant.MessageConstant;
import com.erp.context.BaseContext;
import com.erp.dto.AiChatDTO;
import com.erp.entity.Employee;
import com.erp.exception.AiUnavailableException;
import com.erp.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * AI assistant with tool calling: the model reads the question, asks us to run tools (read-only queries
 * in AiTools), and writes the answer from their results. The loop is capped so a confused model can't
 * keep calling tools (and spending credit) forever.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAssistantService {

    static final int MAX_ROUNDS = 5;
    static final int MAX_HISTORY_TURNS = 10;
    static final int MAX_TEXT_LENGTH = 1000;

    static final String TOO_MANY_STEPS =
            "Sorry, that question needed too many steps. Please try asking it in a simpler way.";

    // Dates are UK dates, whatever time zone the server runs in
    private static final ZoneId UK = ZoneId.of("Europe/London");

    private final LlmClient llm;
    private final AiTools tools;
    private final EmployeeMapper employeeMapper;
    private final ObjectMapper om = new ObjectMapper();

    public String chat(AiChatDTO request) {
        ArrayNode messages = om.createArrayNode();
        messages.addObject().put("role", "system").put("content", systemPrompt());

        // earlier turns, so follow-up questions make sense (only plain user/assistant text, newest kept)
        List<AiChatDTO.Turn> history = request.getHistory() == null ? List.of() : request.getHistory().stream()
                .filter(t -> ("user".equals(t.getRole()) || "assistant".equals(t.getRole()))
                        && t.getContent() != null && !t.getContent().isBlank())
                .toList();
        history.subList(Math.max(0, history.size() - MAX_HISTORY_TURNS), history.size())
                .forEach(t -> messages.addObject().put("role", t.getRole()).put("content", clip(t.getContent())));

        messages.addObject().put("role", "user").put("content", clip(request.getQuestion()));

        ArrayNode toolDefinitions = tools.definitions();
        for (int round = 0; round < MAX_ROUNDS; round++) {
            JsonNode reply = callModel(messages, toolDefinitions);
            JsonNode toolCalls = reply.path("tool_calls");

            if (!toolCalls.isArray() || toolCalls.isEmpty()) {
                return reply.path("content").asString("").trim();
            }

            // keep the model's tool request in the conversation (only the fields the API accepts back),
            // then add one result per call
            ObjectNode toolRequest = messages.addObject();
            toolRequest.put("role", "assistant");
            toolRequest.putNull("content");
            ArrayNode requested = toolRequest.putArray("tool_calls");
            for (JsonNode call : toolCalls) {
                ObjectNode copy = requested.addObject();
                copy.put("id", call.path("id").asString(""));
                copy.put("type", "function");
                copy.putObject("function")
                        .put("name", call.at("/function/name").asString(""))
                        .put("arguments", call.at("/function/arguments").asString(""));
            }
            for (JsonNode call : toolCalls) {
                String name = call.at("/function/name").asString("");
                String result = tools.call(name, call.at("/function/arguments").asString(""));
                log.info("AI assistant: tool {} called", name);
                ObjectNode toolMessage = messages.addObject();
                toolMessage.put("role", "tool");
                toolMessage.put("tool_call_id", call.path("id").asString(""));
                toolMessage.put("content", result);
            }
        }
        return TOO_MANY_STEPS;
    }

    private JsonNode callModel(ArrayNode messages, ArrayNode toolDefinitions) {
        try {
            return llm.chat(messages, toolDefinitions);
        } catch (RuntimeException e) {
            // out of credit, timeout, outage...: a clear message instead of HTTP 500
            log.warn("AI assistant: LLM call failed: {}", e.getMessage());
            throw new AiUnavailableException(MessageConstant.AI_UNAVAILABLE);
        }
    }

    private String systemPrompt() {
        Employee me = employeeMapper.getById(BaseContext.getCurrentId());
        String name = me != null ? me.getName() : "an employee";
        return Prompts.system(LocalDate.now(UK), name, BaseContext.getCurrentRole());
    }

    private static String clip(String text) {
        String t = text == null ? "" : text.trim();
        return t.length() > MAX_TEXT_LENGTH ? t.substring(0, MAX_TEXT_LENGTH) : t;
    }
}

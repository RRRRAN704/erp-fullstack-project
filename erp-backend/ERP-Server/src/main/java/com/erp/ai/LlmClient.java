package com.erp.ai;

import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.Duration;

/**
 * Thin client for an OpenAI-compatible Chat Completions API with tool calling.
 */
@Component
@RequiredArgsConstructor
public class LlmClient {

    @Value("${llm.base-url}") private String baseUrl;
    @Value("${llm.model}")    private String model;
    @Value("${llm.api-key}")  private String apiKey;
    @Value("${llm.timeout-ms:20000}") private int timeoutMs;

    private static final MediaType JSON = MediaType.parse("application/json");

    private final ObjectMapper om = new ObjectMapper();
    private final OkHttpClient http = new OkHttpClient();

    /**
     * Sends the conversation and the tool definitions; returns the model's reply message,
     * which has either "content" (the answer) or "tool_calls" (tools the model wants us to run).
     */
    public JsonNode chat(ArrayNode messages, ArrayNode tools) {
        try {
            ObjectNode root = om.createObjectNode();
            root.put("model", model);
            root.set("messages", messages);
            if (tools != null && !tools.isEmpty()) {
                root.set("tools", tools);
            }

            RequestBody body = RequestBody.create(om.writeValueAsBytes(root), JSON);
            OkHttpClient client = http.newBuilder().callTimeout(Duration.ofMillis(timeoutMs)).build();
            Request req = new Request.Builder()
                    .url(baseUrl + "/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .post(body)
                    .build();

            try (Response resp = client.newCall(req).execute()) {
                String respBody = resp.body() != null ? resp.body().string() : "";
                if (!resp.isSuccessful()) {
                    throw new RuntimeException("LLM HTTP " + resp.code() + " - " + respBody);
                }
                return om.readTree(respBody).at("/choices/0/message");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("LLM API call failed", e);
        }
    }
}

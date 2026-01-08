package com.erp.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LlmClient {

    @Value("${llm.base-url}") private String baseUrl;
    @Value("${llm.model}")    private String model;
    @Value("${llm.api-key}")  private String apiKey;
    @Value("${llm.timeout-ms:12000}") private int timeoutMs;

    // For JSON serialization/deserialization
    private final ObjectMapper om = new ObjectMapper();

    // HTTP client for sending requests
    private final OkHttpClient http = new OkHttpClient();

    /** Make the model strictly return JSON objects (in string form) */
    public String chatToJson(String system, String user) {
        try {
            // Build request payload (JSON body)
            ObjectNode root = om.createObjectNode();
            // specify which LLM model to use
            root.put("model", model);


            // Add system and user messages
            ArrayNode msgs = root.putArray("messages");
            msgs.addObject().put("role", "system").put("content", system);
            msgs.addObject().put("role", "user").put("content", user);

            // Force model to return JSON instead of plain text
            ObjectNode fmt = root.putObject("response_format");
            fmt.put("type", "json_object");


            // Convert payload to RequestBody
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"), om.writeValueAsBytes(root));


            // Build HTTP client with timeout
            OkHttpClient client = http.newBuilder()
                    .callTimeout(java.time.Duration.ofMillis(timeoutMs))
                    .build();

            // Construct HTTP request with headers and body
            Request req = new Request.Builder()
                    .url(baseUrl + "/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .post(body)
                    .build();

            // Execute the request synchronously
            try (Response resp = client.newCall(req).execute()) {
                String respBody = resp.body() != null ? resp.body().string() : "";
                if (!resp.isSuccessful()) {
                    // If API returns error code, throw exception with details
                    throw new RuntimeException("LLM HTTP " + resp.code() + " - " + respBody);
                }
                // Parse response JSON, extract message content field
                JsonNode j = om.readTree(respBody);
                return j.at("/choices/0/message/content").asText("{}");
            }
        } catch (Exception e) {
            // Wrap and rethrow as RuntimeException if anything fails
            throw new RuntimeException("LLM API calls failure", e);
        }
    }
}
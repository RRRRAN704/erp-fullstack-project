package com.erp.controller.employee;

import com.erp.ai.AiAssistantService;
import com.erp.annotation.RequireRole;
import com.erp.constant.MessageConstant;
import com.erp.dto.AiChatDTO;
import com.erp.enumeration.Role;
import com.erp.exception.NoAccessException;
import com.erp.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "AI assistant")
@RequireRole(Role.STAFF)
public class AiController {

    private final AiAssistantService assistant;

    @PostMapping("/chat")
    @Operation(summary = "Ask the AI assistant a question about the business data")
    public Result<Map<String, Object>> chat(@RequestBody AiChatDTO request) {
        if (request.getQuestion() == null || request.getQuestion().isBlank()) {
            throw new NoAccessException(MessageConstant.AI_QUESTION_REQUIRED);
        }
        return Result.success(Map.of("text", assistant.chat(request)));
    }
}

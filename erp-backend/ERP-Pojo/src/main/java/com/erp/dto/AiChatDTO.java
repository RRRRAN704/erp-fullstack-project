package com.erp.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * A question for the AI assistant, with the earlier turns of the conversation (oldest first)
 * so follow-ups like "and last month?" can be understood.
 */
@Data
public class AiChatDTO {

    private String question;

    private List<Turn> history = new ArrayList<>();

    @Data
    public static class Turn {
        /** "user" or "assistant" */
        private String role;
        private String content;
    }
}

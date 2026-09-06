package com.lisa.api.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class ChatDtos {
    private ChatDtos() { }

    public record MessageResponse(long id, String role, String text, String time) { }

    public record ConversationResponse(
            String id,
            String name,
            String initials,
            String status,
            String topic,
            String lastSeen,
            int unread,
            boolean flagged,
            List<MessageResponse> messages) { }

    public record CreateMessageRequest(
            @NotBlank(message = "text is required")
            @Size(max = 4000, message = "text must be at most 4000 characters")
            String text) { }
}

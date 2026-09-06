package com.lisa.api.chat;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.lisa.api.chat.ChatDtos.ConversationResponse;
import static com.lisa.api.chat.ChatDtos.CreateMessageRequest;
import static com.lisa.api.chat.ChatDtos.MessageResponse;

@RestController
@RequestMapping("/api/v1/conversations")
public class ChatController {
    private final ChatRepository repository;

    public ChatController(ChatRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<ConversationResponse> conversations() {
        return repository.findConversations();
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<List<MessageResponse>> messages(@PathVariable String conversationId) {
        if (!validId(conversationId) || !repository.conversationExists(conversationId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repository.findMessages(conversationId));
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<MessageResponse> userMessage(
            @PathVariable String conversationId,
            @Valid @RequestBody CreateMessageRequest request) {
        return addMessage(conversationId, "user", request);
    }

    @PostMapping("/{conversationId}/replies")
    public ResponseEntity<MessageResponse> lisaReply(
            @PathVariable String conversationId,
            @Valid @RequestBody CreateMessageRequest request) {
        return addMessage(conversationId, "lisa", request);
    }

    private ResponseEntity<MessageResponse> addMessage(String conversationId, String role, CreateMessageRequest request) {
        if (!validId(conversationId) || !repository.conversationExists(conversationId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.addMessage(conversationId, role, request.text().trim()));
    }

    private boolean validId(String conversationId) {
        return conversationId != null && conversationId.matches("[a-z0-9-]{1,64}");
    }
}

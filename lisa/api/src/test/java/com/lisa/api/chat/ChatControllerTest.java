package com.lisa.api.chat;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.lisa.api.chat.ChatDtos.ConversationResponse;
import com.lisa.api.chat.ChatDtos.CreateMessageRequest;
import com.lisa.api.chat.ChatDtos.MessageResponse;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {
    @Mock
    private ChatRepository repository;

    @Test
    void conversationsReturnsRepositoryResults() {
        List<ConversationResponse> conversations = List.of(
                new ConversationResponse("conversation-1", "Maya", "M", "online", "Check-in",
                        "10:00 AM", 1, false, List.of()));
        when(repository.findConversations()).thenReturn(conversations);

        List<ConversationResponse> result = new ChatController(repository).conversations();

        assertEquals(conversations, result);
        verify(repository).findConversations();
    }

    @Test
    void messagesReturnsMessagesForExistingConversation() {
        List<MessageResponse> messages = List.of(new MessageResponse(1L, "user", "Hello", "10:00 AM"));
        when(repository.conversationExists("conversation-1")).thenReturn(true);
        when(repository.findMessages("conversation-1")).thenReturn(messages);

        ResponseEntity<List<MessageResponse>> response =
                new ChatController(repository).messages("conversation-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(messages, response.getBody());
        verify(repository).findMessages("conversation-1");
    }

    @Test
    void messagesReturnsNotFoundForInvalidConversationId() {
        ResponseEntity<List<MessageResponse>> response =
                new ChatController(repository).messages("INVALID_ID");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(repository, never()).conversationExists("INVALID_ID");
        verify(repository, never()).findMessages("INVALID_ID");
    }

    @Test
    void messagesReturnsNotFoundForUnknownConversation() {
        when(repository.conversationExists("conversation-1")).thenReturn(false);

        ResponseEntity<List<MessageResponse>> response =
                new ChatController(repository).messages("conversation-1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(repository, never()).findMessages("conversation-1");
    }

    @Test
    void userMessageTrimsTextAndUsesUserRole() {
        MessageResponse savedMessage = new MessageResponse(1L, "user", "Hello", "10:00 AM");
        when(repository.conversationExists("conversation-1")).thenReturn(true);
        when(repository.addMessage("conversation-1", "user", "Hello")).thenReturn(savedMessage);

        ResponseEntity<MessageResponse> response = new ChatController(repository)
                .userMessage("conversation-1", new CreateMessageRequest("  Hello  "));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(savedMessage, response.getBody());
        verify(repository).addMessage("conversation-1", "user", "Hello");
    }

    @Test
    void lisaReplyUsesLisaRole() {
        MessageResponse savedMessage = new MessageResponse(2L, "lisa", "Hi", "10:01 AM");
        when(repository.conversationExists("conversation-1")).thenReturn(true);
        when(repository.addMessage("conversation-1", "lisa", "Hi")).thenReturn(savedMessage);

        ResponseEntity<MessageResponse> response = new ChatController(repository)
                .lisaReply("conversation-1", new CreateMessageRequest("Hi"));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(savedMessage, response.getBody());
        verify(repository).addMessage("conversation-1", "lisa", "Hi");
    }
}
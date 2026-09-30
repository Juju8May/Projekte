package com.lisa.api.student.chat;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.lisa.api.chat.ChatController;
import com.lisa.api.chat.ChatDtos.ConversationResponse;
import com.lisa.api.chat.ChatDtos.CreateMessageRequest;
import com.lisa.api.chat.ChatDtos.MessageResponse;
import com.lisa.api.chat.ChatRepository;

@ExtendWith(MockitoExtension.class)
class ChatControllerStudentTest {
    @Mock
    private ChatRepository repository;

    @InjectMocks
    private ChatController chatController;

    @Test
    void returnListOfConversationsSuccessfully() {
        List<ConversationResponse> conversations = List.of(
                new ConversationResponse("conversation-1", "Simba", "S", "online", "Logged-in",
                        "12:00 AM", 1, false, List.of()));
        when(repository.findConversations()).thenReturn(conversations);

        List<ConversationResponse> result = chatController.conversations();

        assertEquals(conversations, result);
        verify(repository).findConversations();
    }

    @Test
    void messagesReturnOkForValidConvId() {
        String convId = "123";
        List<MessageResponse> messages = List.of(new MessageResponse(1L, "user", "Hello", "10:00 AM"));
        when(repository.conversationExists(convId)).thenReturn(true);
        when(repository.findMessages(convId)).thenReturn(messages);

        ResponseEntity<List<MessageResponse>> response = chatController.messages(convId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(messages, response.getBody());
        verify(repository).conversationExists(convId);
        verify(repository).findMessages(convId);
    }

    @Test
    void messagesReturnNotFound() {
        String convId = "34";
        when(repository.conversationExists(convId)).thenReturn(false);

        ResponseEntity<List<MessageResponse>> response = chatController.messages(convId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(repository).conversationExists(convId);
    }

    @Test
    void userMessageSuccessfully() {
        MessageResponse savedMessage = new MessageResponse(1L, "user", "Hello", "10:00 AM");
        when(repository.conversationExists("conversation-1")).thenReturn(true);
        when(repository.addMessage("conversation-1", "user", "Hello")).thenReturn(savedMessage);

        ResponseEntity<MessageResponse> response = chatController
                .userMessage("conversation-1", new CreateMessageRequest("  Hello  "));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(savedMessage, response.getBody());
        verify(repository).addMessage("conversation-1", "user", "Hello");
    }

    @Test
    void lisaReplySuccessfully() {
        MessageResponse savedMessage = new MessageResponse(2L, "lisa", "Hi", "10:01 AM");
        when(repository.conversationExists("conversation-1")).thenReturn(true);
        when(repository.addMessage("conversation-1", "lisa", "Hi")).thenReturn(savedMessage);

        ResponseEntity<MessageResponse> response = chatController
                .lisaReply("conversation-1", new CreateMessageRequest("Hi"));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(savedMessage, response.getBody());
        verify(repository).addMessage("conversation-1", "lisa", "Hi");
    }
}

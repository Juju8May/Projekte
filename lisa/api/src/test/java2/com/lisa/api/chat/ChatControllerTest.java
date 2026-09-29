package com.lisa.api.chat;

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

import com.lisa.api.chat.ChatDtos.ConversationResponse;
import com.lisa.api.chat.ChatDtos.CreateMessageRequest;
import com.lisa.api.chat.ChatDtos.MessageResponse;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {
    @Mock
    ChatRepository repository;

    @InjectMocks
    ChatController chatController;

    @Test
    void returnListOfConversationsSuccessfully(){
        List<ConversationResponse> conversation = List.of(
            new ConversationResponse("conversation-1", "Simba", "S", "online", "Logged-in",
                "12:00 AM", 1, false, List.of()));
        when(repository.findConversations()).thenReturn(conversation);       
        
        List<ConversationResponse> result = new ChatController(repository).conversations();
        assertEquals(conversation, result);
        verify(repository).findConversations();
    }

    @Test
    void messagesReturnOkForValidConvId(){
        // arrenge
        String convId = "123";
        when(repository.conversationExists(convId)).thenReturn(true);

        ResponseEntity<List<MessageResponse>> response = chatController.messages(convId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(chatController).messages(convId);
    }

    @Test
    void messagesReturnNotFound(){
        // arrange
        String convId = "34";
        when(repository.conversationExists(convId)).thenReturn(false);

        ResponseEntity<List<MessageResponse>> response = chatController.messages(convId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(chatController).messages(convId);
    }

    @Test
    void userMessageSuccessfully(){
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
    void lisaReplySuccessfully(){
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

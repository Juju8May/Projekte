package com.lisa.api.chat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.lisa.api.chat.ChatDtos.ConversationResponse;
import com.lisa.api.chat.ChatDtos.MessageResponse;

@ExtendWith(MockitoExtension.class)
class ChatRepositoryTest {
    @Mock
    private JdbcTemplate jdbc;

    @InjectMocks
    private ChatRepository repository;

    @Test
    void conversationExistsReturnsTrueWhenDatabaseFindsConversation() {
        when(jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM conversations WHERE id = ?)",
                Boolean.class,
                "conversation-1"))
                .thenReturn(true);

        assertTrue(repository.conversationExists("conversation-1"));
    }

    @Test
    void conversationExistsReturnsFalseForNullDatabaseResult() {
        when(jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM conversations WHERE id = ?)",
                Boolean.class,
                "missing"))
                .thenReturn(null);

        assertFalse(repository.conversationExists("missing"));
    }

    @Test
    void findMessagesMapsDatabaseRows() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getLong("id")).thenReturn(7L);
        when(resultSet.getString("role")).thenReturn("lisa");
        when(resultSet.getString("text")).thenReturn("Hello");
        when(resultSet.getString("time")).thenReturn("10:00 AM");
        when(jdbc.query(
                eq("""
                        SELECT id, role, text, to_char(created_at, 'HH12:MI AM') AS time
                        FROM messages
                        WHERE conversation_id = ?
                        ORDER BY created_at ASC, id ASC
                        """),
                any(RowMapper.class),
                eq("conversation-1")))
                .thenAnswer(invocation -> {
                    RowMapper<MessageResponse> mapper = invocation.getArgument(1);
                    return List.of(mapper.mapRow(resultSet, 0));
                });

        List<MessageResponse> messages = repository.findMessages("conversation-1");

        assertEquals(List.of(new MessageResponse(7L, "lisa", "Hello", "10:00 AM")), messages);
    }

    @Test
    void addMessagePassesConversationRoleAndTextToDatabase() {
        MessageResponse savedMessage = new MessageResponse(8L, "user", "Hi", "10:01 AM");
        when(jdbc.queryForObject(
                eq("""
                        INSERT INTO messages (conversation_id, role, text, read_at)
                        VALUES (?, ?, ?, CASE WHEN ? = 'lisa' THEN NOW() ELSE NULL END)
                        RETURNING id, role, text, to_char(created_at, 'HH12:MI AM') AS time
                        """),
                any(RowMapper.class),
                eq("conversation-1"), eq("user"), eq("Hi"), eq("user")))
                .thenReturn(savedMessage);

        MessageResponse result = repository.addMessage("conversation-1", "user", "Hi");

        assertEquals(savedMessage, result);
        verify(jdbc).queryForObject(
                eq("""
                        INSERT INTO messages (conversation_id, role, text, read_at)
                        VALUES (?, ?, ?, CASE WHEN ? = 'lisa' THEN NOW() ELSE NULL END)
                        RETURNING id, role, text, to_char(created_at, 'HH12:MI AM') AS time
                        """),
                any(RowMapper.class),
                eq("conversation-1"), eq("user"), eq("Hi"), eq("user"));
    }

    @Test
    void findConversationsMapsConversationAndLoadsMessages() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("id")).thenReturn("conversation-1");
        when(resultSet.getString("name")).thenReturn("Maya");
        when(resultSet.getString("initials")).thenReturn("M");
        when(resultSet.getString("status")).thenReturn("online");
        when(resultSet.getString("topic")).thenReturn("Check-in");
        when(resultSet.getString("last_seen")).thenReturn("10:00 AM");
        when(resultSet.getInt("unread")).thenReturn(2);
        when(resultSet.getBoolean("flagged")).thenReturn(true);
        when(jdbc.query(
                eq("""
                        SELECT id, role, text, to_char(created_at, 'HH12:MI AM') AS time
                        FROM messages
                        WHERE conversation_id = ?
                        ORDER BY created_at ASC, id ASC
                        """),
                any(RowMapper.class),
                eq("conversation-1")))
                .thenReturn(List.of(new MessageResponse(1L, "user", "Hi", "09:59 AM")));
        when(jdbc.query(
                eq("""
                        SELECT c.id, c.name, c.initials, c.status, c.topic,
                               COALESCE(to_char(MAX(m.created_at), 'HH12:MI AM'), 'No messages') AS last_seen,
                               COUNT(m.id) FILTER (WHERE m.role = 'user' AND m.read_at IS NULL)::int AS unread,
                               c.flagged
                        FROM conversations c
                        LEFT JOIN messages m ON m.conversation_id = c.id
                        GROUP BY c.id
                        ORDER BY MAX(m.created_at) DESC NULLS LAST
                        """),
                any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    RowMapper<ConversationResponse> mapper = invocation.getArgument(1);
                    return List.of(mapper.mapRow(resultSet, 0));
                });

        List<ConversationResponse> conversations = repository.findConversations();

        assertEquals(1, conversations.size());
        assertEquals("conversation-1", conversations.get(0).id());
        assertEquals(2, conversations.get(0).unread());
        assertTrue(conversations.get(0).flagged());
        assertEquals(1, conversations.get(0).messages().size());
    }
}

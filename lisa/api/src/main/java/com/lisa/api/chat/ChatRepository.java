package com.lisa.api.chat;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.lisa.api.chat.ChatDtos.ConversationResponse;
import static com.lisa.api.chat.ChatDtos.MessageResponse;

@Repository
public class ChatRepository {
    private final JdbcTemplate jdbc;

    public ChatRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<ConversationResponse> findConversations() {
        return jdbc.query("""
                SELECT c.id, c.name, c.initials, c.status, c.topic,
                       COALESCE(to_char(MAX(m.created_at), 'HH12:MI AM'), 'No messages') AS last_seen,
                       COUNT(m.id) FILTER (WHERE m.role = 'user' AND m.read_at IS NULL)::int AS unread,
                       c.flagged
                FROM conversations c
                LEFT JOIN messages m ON m.conversation_id = c.id
                GROUP BY c.id
                ORDER BY MAX(m.created_at) DESC NULLS LAST
                """, (rs, rowNum) -> new ConversationResponse(
                rs.getString("id"), rs.getString("name"), rs.getString("initials"),
                rs.getString("status"), rs.getString("topic"), rs.getString("last_seen"),
                rs.getInt("unread"), rs.getBoolean("flagged"), findMessages(rs.getString("id"))));
    }

    public List<MessageResponse> findMessages(String conversationId) {
        return jdbc.query("""
                SELECT id, role, text, to_char(created_at, 'HH12:MI AM') AS time
                FROM messages
                WHERE conversation_id = ?
                ORDER BY created_at ASC, id ASC
                """, (rs, rowNum) -> new MessageResponse(
                rs.getLong("id"), rs.getString("role"), rs.getString("text"), rs.getString("time")), conversationId);
    }

    public MessageResponse addMessage(String conversationId, String role, String text) {
        return jdbc.queryForObject("""
                INSERT INTO messages (conversation_id, role, text, read_at)
                VALUES (?, ?, ?, CASE WHEN ? = 'lisa' THEN NOW() ELSE NULL END)
                RETURNING id, role, text, to_char(created_at, 'HH12:MI AM') AS time
                """, (rs, rowNum) -> new MessageResponse(
                rs.getLong("id"), rs.getString("role"), rs.getString("text"), rs.getString("time")),
                conversationId, role, text, role);
    }

    public boolean conversationExists(String conversationId) {
        Boolean exists = jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM conversations WHERE id = ?)", Boolean.class, conversationId);
        return Boolean.TRUE.equals(exists);
    }
}

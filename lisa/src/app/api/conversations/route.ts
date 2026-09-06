import { NextResponse } from "next/server";
import { db } from "../../../lib/db";
import { hasValidAdminSession } from "../../../lib/adminSession";
import { getUserSession } from "../../../lib/userSession";
import { NextRequest } from "next/server";

export const dynamic = "force-dynamic";

type ConversationRow = {
  id: string;
  name: string;
  initials: string;
  status: "online" | "away" | "offline";
  topic: string;
  last_seen: string;
  unread: number;
  flagged: boolean;
};

export async function GET(request: NextRequest) {
  try {
    const isAdmin = await hasValidAdminSession(request);
    const userSession = isAdmin ? null : await getUserSession(request);
    if (!isAdmin && !userSession) return NextResponse.json({ error: "Authentication required" }, { status: 401 });
    const result = await db.query<ConversationRow>(`
      SELECT c.id, c.name, c.initials, c.status, c.topic,
        COALESCE(to_char(MAX(m.created_at), 'HH12:MI AM'), 'No messages') AS last_seen,
        COUNT(m.id) FILTER (WHERE m.role = 'user' AND m.read_at IS NULL)::int AS unread,
        c.flagged
      FROM conversations c
      LEFT JOIN messages m ON m.conversation_id = c.id
      WHERE ($1 = TRUE OR c.id = $2)
      GROUP BY c.id
      ORDER BY MAX(m.created_at) DESC NULLS LAST
    `, [isAdmin, userSession?.conversationId ?? ""]);

    const conversations = await Promise.all(result.rows.map(async (conversation) => {
      const messages = await db.query(`
        SELECT id, role, text, to_char(created_at, 'HH12:MI AM') AS time
        FROM messages
        WHERE conversation_id = $1
        ORDER BY created_at ASC, id ASC
      `, [conversation.id]);
      return { ...conversation, messages: messages.rows };
    }));

    return NextResponse.json(conversations);
  } catch (error) {
    console.error("Unable to load conversations", error);
    return NextResponse.json({ error: "Database unavailable" }, { status: 503 });
  }
}

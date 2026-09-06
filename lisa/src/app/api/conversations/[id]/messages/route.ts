import { NextRequest, NextResponse } from "next/server";
import { db } from "../../../../../lib/db";
import { hasValidAdminSession } from "../../../../../lib/adminSession";
import { getUserSession } from "../../../../../lib/userSession";

export const dynamic = "force-dynamic";

export async function GET(_request: NextRequest, context: { params: Promise<{ id: string }> }) {
  const { id } = await context.params;
  try {
    const isAdmin = await hasValidAdminSession(_request);
    const userSession = isAdmin ? null : await getUserSession(_request);
    if (!isAdmin && (!userSession || userSession.conversationId !== id)) {
      return NextResponse.json({ error: "Conversation not available" }, { status: 403 });
    }
    const result = await db.query(`
      SELECT id, role, text, to_char(created_at, 'HH12:MI AM') AS time
      FROM messages
      WHERE conversation_id = $1
      ORDER BY created_at ASC, id ASC
    `, [id]);
    await db.query("UPDATE messages SET read_at = NOW() WHERE conversation_id = $1 AND role = 'lisa' AND read_at IS NULL", [id]);
    return NextResponse.json(result.rows);
  } catch (error) {
    console.error("Unable to load messages", error);
    return NextResponse.json({ error: "Database unavailable" }, { status: 503 });
  }
}

export async function POST(request: NextRequest, context: { params: Promise<{ id: string }> }) {
  const { id } = await context.params;
  const body = await request.json().catch(() => null);
  const role = body?.role;
  const text = typeof body?.text === "string" ? body.text.trim() : "";

  if (!["user", "lisa"].includes(role) || !text) {
    return NextResponse.json({ error: "role and text are required" }, { status: 400 });
  }

  try {
    const isAdmin = await hasValidAdminSession(request);
    const userSession = isAdmin ? null : await getUserSession(request);
    if (role === "lisa" && !isAdmin) {
      return NextResponse.json({ error: "Admin session required" }, { status: 403 });
    }
    if (role === "user" && (!userSession || userSession.conversationId !== id)) {
      return NextResponse.json({ error: "Conversation not available" }, { status: 403 });
    }
    const result = await db.query(`
      INSERT INTO messages (conversation_id, role, text, read_at)
      VALUES ($1, $2, $3, CASE WHEN $2 = 'lisa' THEN NOW() ELSE NULL END)
      RETURNING id, role, text, to_char(created_at, 'HH12:MI AM') AS time
    `, [id, role, text]);
    return NextResponse.json(result.rows[0], { status: 201 });
  } catch (error) {
    console.error("Unable to save message", error);
    return NextResponse.json({ error: "Database unavailable" }, { status: 503 });
  }
}

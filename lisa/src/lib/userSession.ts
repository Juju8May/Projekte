import { NextRequest } from "next/server";

export async function getUserSession(request: NextRequest) {
  const token = request.cookies.get("lisa_user_session")?.value;
  if (!token) return null;
  try {
    const response = await fetch(`${process.env.JAVA_API_URL ?? "http://localhost:8080"}/api/v1/user-auth/session`, {
      method: "POST",
      headers: { Authorization: `Bearer ${token}`, "X-Api-Key": process.env.LISA_API_KEY ?? "" },
      cache: "no-store",
    });
    if (!response.ok) return null;
    const session = await response.json() as { authenticated?: boolean; conversationId?: string };
    return session.authenticated && session.conversationId ? session : null;
  } catch {
    return null;
  }
}

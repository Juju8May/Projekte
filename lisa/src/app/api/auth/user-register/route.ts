import { NextResponse } from "next/server";

export async function POST(request: Request) {
  const body = await request.json().catch(() => null);
  const username = typeof body?.username === "string" ? body.username.trim() : "";
  const password = typeof body?.password === "string" ? body.password : "";
  if (!/^[a-zA-Z0-9_-]{3,40}$/.test(username) || password.length < 8 || password.length > 200) {
    return NextResponse.json({ error: "Choose a username and a password with at least 8 characters." }, { status: 400 });
  }

  try {
    const response = await fetch(`${process.env.JAVA_API_URL ?? "http://localhost:8080"}/api/v1/user-auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json", "X-Api-Key": process.env.LISA_API_KEY ?? "" },
      body: JSON.stringify({ username, password }),
      cache: "no-store",
    });
    const result = await response.json().catch(() => ({ error: "Registration failed" }));
    if (!response.ok) return NextResponse.json({ error: result.error ?? "Registration failed" }, { status: response.status });
    const nextResponse = NextResponse.json({ authenticated: true });
    nextResponse.cookies.set("lisa_user_session", result.token, {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "strict",
      path: "/",
      maxAge: result.expiresInSeconds,
    });
    return nextResponse;
  } catch {
    return NextResponse.json({ error: "Authentication service unavailable" }, { status: 503 });
  }
}

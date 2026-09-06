import { NextResponse } from "next/server";

export async function POST(request: Request) {
  const body = await request.json().catch(() => null);
  const username = typeof body?.username === "string" ? body.username : "";
  const password = typeof body?.password === "string" ? body.password : "";
  if (!username || !password || username.length > 100 || password.length > 200) {
    return NextResponse.json({ error: "Invalid credentials" }, { status: 400 });
  }

  try {
    const response = await fetch(`${process.env.JAVA_API_URL ?? "http://localhost:8080"}/api/v1/user-auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json", "X-Api-Key": process.env.LISA_API_KEY ?? "" },
      body: JSON.stringify({ username, password }),
      cache: "no-store",
    });
    if (!response.ok) return NextResponse.json({ error: "Invalid credentials" }, { status: 401 });
    const result = await response.json();
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

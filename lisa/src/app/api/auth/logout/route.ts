import { NextResponse } from "next/server";

export async function POST() {
  const response = NextResponse.json({ authenticated: false });
  response.cookies.set("lisa_session", "", { httpOnly: true, expires: new Date(0), sameSite: "strict", path: "/" });
  response.cookies.set("lisa_user_session", "", { httpOnly: true, expires: new Date(0), sameSite: "strict", path: "/" });
  return response;
}

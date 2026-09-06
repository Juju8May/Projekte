import { NextRequest } from "next/server";

export async function hasValidAdminSession(request: NextRequest) {
  const token = request.cookies.get("lisa_session")?.value;
  if (!token) return false;
  try {
    const response = await fetch(`${process.env.JAVA_API_URL ?? "http://localhost:8080"}/api/v1/auth/session`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "X-Api-Key": process.env.LISA_API_KEY ?? "",
      },
      cache: "no-store",
    });
    if (!response.ok) return false;
    const result = await response.json() as { authenticated?: boolean };
    return result.authenticated === true;
  } catch {
    return false;
  }
}

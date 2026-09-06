import { NextRequest, NextResponse } from "next/server";
import { hasValidAdminSession } from "./lib/adminSession";
import { getUserSession } from "./lib/userSession";

export async function middleware(request: NextRequest) {
  if (request.nextUrl.pathname.startsWith("/dev") && request.nextUrl.pathname !== "/dev/login") {
    if (!(await hasValidAdminSession(request))) {
      return NextResponse.redirect(new URL("/dev/login", request.url));
    }
  }
  if (request.nextUrl.pathname === "/" && !(await getUserSession(request))) {
    return NextResponse.redirect(new URL("/login", request.url));
  }
  return NextResponse.next();
}

export const config = {
   matcher: ["/", "/dev/:path*"],
};

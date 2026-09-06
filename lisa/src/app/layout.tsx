import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Lisa | Your private companion",
  description: "A private, fictional AI companion prototype.",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}

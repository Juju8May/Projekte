"use client";

import { FormEvent, useState } from "react";
import Link from "next/link";
import { ArrowRight, LockKeyhole, Sparkles } from "lucide-react";

export default function DevLoginPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function login(event: FormEvent) {
    event.preventDefault();
    setError("");
    setLoading(true);
    try {
      const response = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });
      if (!response.ok) throw new Error();
      window.location.assign("/dev");
    } catch {
      setError("Login failed. Check your credentials.");
      setLoading(false);
    }
  }

  return (
    <main className="dev-login-shell">
      <section className="dev-login-card">
        <div className="dev-login-mark"><Sparkles size={18} /></div>
        <p className="dev-eyebrow">Private workspace</p>
        <h1>Welcome back.</h1>
        <p className="dev-login-copy">Sign in to manage conversations and reply as Lisa.</p>
        <form onSubmit={login} className="dev-login-form">
          <label>Username<input value={username} onChange={(event) => setUsername(event.target.value)} autoComplete="username" required /></label>
          <label>Password<input value={password} onChange={(event) => setPassword(event.target.value)} type="password" autoComplete="current-password" required /></label>
          {error && <p className="dev-login-error" role="alert">{error}</p>}
          <button type="submit" disabled={loading}><LockKeyhole size={15} /> {loading ? "Signing in..." : "Enter studio"}<ArrowRight size={16} /></button>
        </form>
        <Link className="dev-login-back" href="/">Back to Lisa</Link>
      </section>
    </main>
  );
}

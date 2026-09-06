"use client";

import { FormEvent, useState } from "react";
import Link from "next/link";
import { ArrowRight, LockKeyhole, Sparkles } from "lucide-react";

export default function UserLoginPage() {
  const [isRegistering, setIsRegistering] = useState(false);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function login(event: FormEvent) {
    event.preventDefault();
    setError("");
    setLoading(true);
    try {
      const response = await fetch(isRegistering ? "/api/auth/user-register" : "/api/auth/user-login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });
      if (!response.ok) {
        const result = await response.json().catch(() => null);
        throw new Error(result?.error ?? "Authentication failed");
      }
      window.location.assign("/");
    } catch (caughtError) {
      setError(caughtError instanceof Error ? caughtError.message : "Authentication failed. Check your details.");
      setLoading(false);
    }
  }

  return (
    <main className="user-login-shell">
      <section className="user-login-visual">
        <Link className="user-login-brand" href="/"><span className="user-login-mark"><Sparkles size={16} /></span><span>LISA</span></Link>
        <div className="user-login-quote"><p className="welcome-kicker">A quiet place to land</p><h1>Come as you are.<br /><em>Stay awhile.</em></h1><p>Thoughtful conversation, held softly and kept private on your device.</p></div>
        <div className="user-login-footer"><span>Private by design</span><span className="user-login-line" /><span>Lisa / 01</span></div>
      </section>
      <section className="user-login-panel">
        <div className="user-login-card">
          <div className="user-login-heading"><p className="user-login-overline">Your private space</p><h2>{isRegistering ? "Make room for you." : "Welcome back."}</h2><p>{isRegistering ? "Create your private space and start a conversation with Lisa." : "Sign in to continue your conversation with Lisa."}</p></div>
          <form onSubmit={login} className="user-login-form">
            <label>Username<input value={username} onChange={(event) => setUsername(event.target.value)} autoComplete="username" placeholder="Your username" required /></label>
            <label>Password<input value={password} onChange={(event) => setPassword(event.target.value)} type="password" autoComplete={isRegistering ? "new-password" : "current-password"} placeholder={isRegistering ? "At least 8 characters" : "Your password"} minLength={isRegistering ? 8 : undefined} required /></label>
            {error && <span className="user-login-error" role="alert">{error}</span>}
            <button type="submit" disabled={loading}><LockKeyhole size={15} /> {loading ? "Please wait..." : isRegistering ? "Create account" : "Continue"}<ArrowRight size={16} /></button>
          </form>
          <div className="user-login-note"><span className="user-login-check"><Sparkles size={11} /></span><span>Your conversation stays private on this device.</span></div>
          <div className="user-login-links"><button type="button" onClick={() => { setIsRegistering((current) => !current); setError(""); }}>{isRegistering ? "Already have an account? Sign in" : "New here? Create an account"}</button><Link className="user-login-team" href="/dev/login">Team login <ArrowRight size={13} /></Link></div>
        </div>
      </section>
    </main>
  );
}

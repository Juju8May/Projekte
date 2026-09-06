"use client";

import { ChangeEvent, FormEvent, useEffect, useRef, useState } from "react";
import {
  ArrowUp,
  ChevronDown,
  Heart,
  ImagePlus,
  Info,
  Menu,
  MoreHorizontal,
  Plus,
  Sparkles,
  Trash2,
  X,
} from "lucide-react";
import { listenForConversationChanges, notifyConversationChanged } from "../lib/chatEvents";

type Message = { id: number; role: "user" | "lisa"; text: string; time: string; imageUrl?: string; imageAlt?: string };

const starterMessages: Message[] = [
  {
    id: 1,
    role: "lisa",
    text: "Hey, I’m Lisa. I’m really glad you’re here. What’s on your mind today?",
    time: "9:41 AM",
  },
];

const suggestions = ["Help me clear my head", "Tell me something sweet", "I had a rough day"];

function currentTime() {
  return new Intl.DateTimeFormat("en-US", { hour: "numeric", minute: "2-digit" }).format(new Date());
}

export default function Home() {
  const [messages, setMessages] = useState<Message[]>(starterMessages);
  const [draft, setDraft] = useState("");
  const [isTyping, setIsTyping] = useState(false);
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const [showSidebar, setShowSidebar] = useState(false);
  const conversationId = "maya";
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const imageInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    let cancelled = false;
    async function loadMessages() {
      try {
        const response = await fetch(`/api/conversations/${conversationId}/messages`, { cache: "no-store" });
        if (!response.ok) return;
        const databaseMessages = await response.json();
        if (!cancelled && databaseMessages.length > 0) setMessages(databaseMessages);
      } catch {
        // The local starter view remains available while the database is offline.
      }
    }
    loadMessages();
    const stopListening = listenForConversationChanges((changedConversationId) => {
      if (changedConversationId === conversationId) loadMessages();
    });
    return () => {
      cancelled = true;
      stopListening();
    };
  }, []);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, isTyping]);

  async function sendMessage(event?: FormEvent) {
    event?.preventDefault();
    const text = draft.trim();
    if (!text || isTyping) return;
    setDraft("");
    setIsTyping(true);
    try {
      await fetch(`/api/conversations/${conversationId}/messages`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ role: "user", text }),
      });
      notifyConversationChanged(conversationId);
    } finally {
      setIsTyping(false);
    }
  }

  function sendUserImage(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    event.target.value = "";
    if (!file || !file.type.startsWith("image/") || isTyping) return;
    const reader = new FileReader();
    reader.onload = () => {
      if (typeof reader.result !== "string") return;
      const imageData = reader.result;
      setMessages((current) => [...current, { id: Date.now(), role: "user", text: "I sent you an image.", time: currentTime(), imageUrl: imageData, imageAlt: file.name }]);
      fetch(`/api/conversations/${conversationId}/messages`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ role: "user", text: "I sent you an image." }),
      }).then(() => notifyConversationChanged(conversationId)).catch(() => undefined);
    };
    reader.readAsDataURL(file);
  }

  function clearConversation() {
    setMessages(starterMessages);
  }

  return (
    <main className="app-shell">
      <div className={`sidebar-backdrop ${showSidebar ? "is-visible" : ""}`} onClick={() => setShowSidebar(false)} />
      <aside className={`sidebar ${showSidebar ? "is-open" : ""}`}>
        <div className="brand"><div className="brand-mark"><Sparkles size={16} /></div><span>LISA</span></div>
        <button className="new-chat" onClick={clearConversation}><Plus size={17} /> New conversation</button>
        <div className="side-label">Today</div>
        <button className="history-item active"><span className="history-dot" />A little check-in</button>
        <div className="side-label older">Earlier</div>
        <button className="history-item"><span className="history-dot muted" />Getting to know you</button>
        <div className="sidebar-bottom">
          <button className="side-action" onClick={() => setIsProfileOpen(true)}><Heart size={17} /> About Lisa <ChevronDown size={15} /></button>
          <p className="privacy-note"><span className="privacy-dot" />Private on this device</p>
        </div>
      </aside>

      <section className="chat-panel">
        <header className="topbar">
          <button className="icon-button mobile-menu" aria-label="Open menu" onClick={() => setShowSidebar(true)}><Menu size={20} /></button>
          <div className="mobile-title"><span>LISA</span><small>your private companion</small></div>
          <div className="presence"><span className="online-dot" /> Lisa is here</div>
          <div className="topbar-actions"><button className="icon-button" aria-label="Conversation information"><Info size={19} /></button><button className="icon-button" aria-label="More options"><MoreHorizontal size={20} /></button></div>
        </header>

        <div className="conversation">
          <div className="conversation-inner">
            <div className="day-divider"><span>Today</span></div>
            <div className="welcome-block">
              <div className="avatar large"><div className="portrait portrait-large"><span>l</span></div><span className="avatar-status" /></div>
              <p className="welcome-kicker">A quiet place to land</p>
              <h1>Talk to me about<br /><em>anything.</em></h1>
              <p className="welcome-copy">No pressure. No judgment. Just a little space for whatever is on your mind.</p>
            </div>
            <div className="messages">
              {messages.map((message) => <div className={`message-row ${message.role}`} key={message.id}>
                {message.role === "lisa" && <div className="avatar small"><div className="portrait"><span>l</span></div><span className="avatar-status" /></div>}
                <div>{message.imageUrl && <img className="message-image" src={message.imageUrl} alt={message.imageAlt} />}<div className="message-bubble">{message.text}</div><span className="message-time">{message.time}</span></div>
              </div>)}
              {isTyping && <div className="message-row lisa"><div className="avatar small"><div className="portrait"><span>l</span></div><span className="avatar-status" /></div><div className="message-bubble typing"><i /><i /><i /></div></div>}
              <div ref={messagesEndRef} />
            </div>
            {messages.length < 3 && <div className="suggestions">{suggestions.map((suggestion) => <button key={suggestion} onClick={() => setDraft(suggestion)}>{suggestion}</button>)}</div>}
          </div>
        </div>

        <div className="composer-wrap">
          <form className="composer" onSubmit={sendMessage}>
            <button type="button" className="composer-icon" aria-label="Attach your image" onClick={() => imageInputRef.current?.click()}><ImagePlus size={19} /></button>
            <input ref={imageInputRef} className="hidden-image-input" type="file" accept="image/*" onChange={sendUserImage} />
            <input value={draft} onChange={(event) => setDraft(event.target.value)} placeholder="Say something to Lisa..." aria-label="Message Lisa" />
            <button type="submit" className="send-button" aria-label="Send message"><ArrowUp size={19} /></button>
          </form>
          <p className="composer-caption">Lisa is fictional and designed for thoughtful conversation. <button onClick={() => setIsProfileOpen(true)}>Learn more</button></p>
        </div>
      </section>

      {isProfileOpen && <div className="profile-overlay" onClick={() => setIsProfileOpen(false)}><aside className="profile-drawer" onClick={(event) => event.stopPropagation()}><button className="close-button" onClick={() => setIsProfileOpen(false)} aria-label="Close"><X size={19} /></button><div className="drawer-portrait portrait portrait-drawer"><span>l</span></div><p className="welcome-kicker">Your companion</p><h2>Meet Lisa</h2><p className="drawer-copy">Lisa is a fictional, 21+ AI companion made for warm, honest, everyday conversation. She remembers this chat on your device only.</p><div className="drawer-line"><Heart size={17} /><span>Warm, curious, judgment-free</span></div><div className="drawer-line"><Sparkles size={17} /><span>Made for your quiet moments</span></div><button className="clear-button" onClick={clearConversation}><Trash2 size={16} /> Clear this conversation</button></aside></div>}
    </main>
  );
}

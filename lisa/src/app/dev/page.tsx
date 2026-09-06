"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { listenForConversationChanges, notifyConversationChanged } from "../../lib/chatEvents";
import {
  Archive,
  ArrowUp,
  Bell,
  Check,
  ChevronDown,
  Filter,
  Flag,
  MoreHorizontal,
  Search,
  ShieldCheck,
  Sparkles,
  Tag,
} from "lucide-react";

type ChatMessage = {
  id: number;
  role: "user" | "lisa";
  text: string;
  time: string;
};

type Chat = {
  id: string;
  name: string;
  initials: string;
  status: "online" | "away" | "offline";
  topic: string;
  lastSeen: string;
  unread: number;
  flagged: boolean;
  messages: ChatMessage[];
};

const initialChats: Chat[] = [
  {
    id: "maya",
    name: "Maya R.",
    initials: "MR",
    status: "online",
    topic: "A little check-in",
    lastSeen: "Just now",
    unread: 2,
    flagged: false,
    messages: [
      { id: 1, role: "user", text: "I had a rough day at work and I cannot switch off.", time: "10:18 AM" },
      { id: 2, role: "lisa", text: "That sounds like a lot to carry home with you. What part is still looping in your head?", time: "10:18 AM" },
      { id: 3, role: "user", text: "The feeling that I should have done more.", time: "10:19 AM" },
    ],
  },
  {
    id: "jonah",
    name: "Jonah P.",
    initials: "JP",
    status: "away",
    topic: "Getting to know you",
    lastSeen: "8 min ago",
    unread: 0,
    flagged: true,
    messages: [
      { id: 4, role: "user", text: "Do you remember what I told you yesterday?", time: "10:10 AM" },
      { id: 5, role: "lisa", text: "I remember what we have in this conversation. You were thinking about making more room for your music.", time: "10:11 AM" },
    ],
  },
  {
    id: "lena",
    name: "Lena K.",
    initials: "LK",
    status: "offline",
    topic: "A softer morning",
    lastSeen: "Yesterday",
    unread: 0,
    flagged: false,
    messages: [
      { id: 6, role: "user", text: "I finally slept through the night.", time: "Yesterday" },
      { id: 7, role: "lisa", text: "I am really glad your body got that rest. How does the day feel from there?", time: "Yesterday" },
    ],
  },
];

const responseSuggestions = ["I hear you. Tell me more.", "Let’s take this one step at a time.", "What would feel supportive right now?"];

export default function DevPage() {
  const [chats, setChats] = useState(initialChats);
  const [activeId, setActiveId] = useState("maya");
  const [query, setQuery] = useState("");
  const [draft, setDraft] = useState("");
  const [filter, setFilter] = useState<"all" | "flagged">("all");
  const [isResponding, setIsResponding] = useState(false);
  const activeChat = chats.find((chat) => chat.id === activeId) ?? chats[0];

  useEffect(() => {
    let cancelled = false;
    async function loadChats() {
      try {
        const response = await fetch("/api/conversations", { cache: "no-store" });
        if (!response.ok) return;
        const databaseChats: Array<Chat & { last_seen: string }> = await response.json();
        if (!cancelled && databaseChats.length > 0) {
          setChats(databaseChats.map((chat) => ({ ...chat, lastSeen: chat.last_seen })));
        }
      } catch {
        // Keep the last visible state while the database reconnects.
      }
    }
    loadChats();
    const stopListening = listenForConversationChanges(() => loadChats());
    return () => {
      cancelled = true;
      stopListening();
    };
  }, []);

  const visibleChats = useMemo(() => chats.filter((chat) => {
    const matchesQuery = `${chat.name} ${chat.topic}`.toLowerCase().includes(query.toLowerCase());
    return matchesQuery && (filter === "all" || chat.flagged);
  }), [chats, filter, query]);

  function selectChat(id: string) {
    setActiveId(id);
    setChats((current) => current.map((chat) => chat.id === id ? { ...chat, unread: 0 } : chat));
  }

  async function sendMessage(event?: FormEvent) {
    event?.preventDefault();
    const text = draft.trim();
    if (!text || isResponding) return;
    setIsResponding(true);
    try {
      const response = await fetch(`/api/conversations/${activeChat.id}/messages`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ role: "lisa", text }),
      });
      if (!response.ok) throw new Error("Message could not be saved");
      const message = await response.json();
      setChats((current) => current.map((chat) => chat.id === activeChat.id ? {
        ...chat,
        lastSeen: "Just now",
        messages: [...chat.messages, message],
      } : chat));
      notifyConversationChanged(activeChat.id);
      setDraft("");
    } finally {
      setIsResponding(false);
    }
  }

  function toggleFlag() {
    setChats((current) => current.map((chat) => chat.id === activeChat.id ? { ...chat, flagged: !chat.flagged } : chat));
  }

  function sendSuggestion(suggestion: string) {
    setDraft(suggestion);
  }

  function addReply() {
    if (isResponding) return;
    setIsResponding(true);
    window.setTimeout(() => {
      const reply = "Thank you for telling me. I am here with you in this moment.";
      sendMessageWithText(reply);
      setIsResponding(false);
    }, 500);
  }

  async function sendMessageWithText(text: string) {
    try {
      const response = await fetch(`/api/conversations/${activeChat.id}/messages`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ role: "lisa", text }),
      });
      if (!response.ok) return;
      const message = await response.json();
      setChats((current) => current.map((chat) => chat.id === activeChat.id ? { ...chat, messages: [...chat.messages, message] } : chat));
      notifyConversationChanged(activeChat.id);
    } catch {
      // Keep the studio usable while the database reconnects.
    }
  }

  return (
    <main className="dev-shell">
      <header className="dev-topbar">
        <Link className="dev-brand" href="/"><span className="dev-mark"><Sparkles size={15} /></span><span>LISA <b>STUDIO</b></span></Link>
        <div className="dev-environment"><span className="live-dot" /> Local workspace <span className="dev-divider" /> v0.1.0</div>
        <div className="dev-actions"><button className="dev-icon-button" aria-label="Notifications"><Bell size={17} /><i /></button><button className="dev-user"><span>JL</span><strong>Julia</strong><ChevronDown size={14} /></button></div>
      </header>

      <div className="dev-layout">
        <aside className="chat-inbox">
          <div className="inbox-heading"><div><p className="dev-eyebrow">Workspace</p><h1>Conversations</h1></div><button className="dev-icon-button" aria-label="More conversation options"><MoreHorizontal size={18} /></button></div>
          <div className="inbox-stats"><span><b>{chats.length}</b> active chats</span><span><b>{chats.reduce((total, chat) => total + chat.unread, 0)}</b> unread</span></div>
          <label className="dev-search"><Search size={16} /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search conversations" /><kbd>/</kbd></label>
          <div className="inbox-filter"><button className={filter === "all" ? "selected" : ""} onClick={() => setFilter("all")}>All chats</button><button className={filter === "flagged" ? "selected" : ""} onClick={() => setFilter("flagged")}><Flag size={13} /> Flagged</button><button aria-label="Filter options"><Filter size={14} /></button></div>
          <div className="chat-list">{visibleChats.map((chat) => <button className={`chat-list-item ${chat.id === activeId ? "active" : ""}`} key={chat.id} onClick={() => selectChat(chat.id)}><span className="user-avatar">{chat.initials}<i className={chat.status} /></span><span className="chat-list-copy"><strong>{chat.name}</strong><small>{chat.topic}</small></span><span className="chat-list-meta"><time>{chat.lastSeen}</time>{chat.unread > 0 && <b>{chat.unread}</b>}</span></button>)}{visibleChats.length === 0 && <p className="empty-list">No conversations match this filter.</p>}</div>
          <button className="load-more">Load older conversations <ChevronDown size={14} /></button>
        </aside>

        <section className="dev-conversation">
          <header className="conversation-head"><div className="conversation-person"><span className="user-avatar large">{activeChat.initials}<i className={activeChat.status} /></span><div><h2>{activeChat.name}</h2><p><span className="live-dot" /> {activeChat.status === "online" ? "Active now" : `Last active ${activeChat.lastSeen.toLowerCase()}`}</p></div></div><div className="conversation-controls"><span className="topic-tag"><Tag size={13} /> {activeChat.topic}</span><button className={`control-button ${activeChat.flagged ? "marked" : ""}`} onClick={toggleFlag} aria-label="Flag conversation"><Flag size={17} /></button><button className="control-button" aria-label="More conversation actions"><MoreHorizontal size={18} /></button></div></header>
          <div className="dev-message-area"><div className="date-rule"><span>Today</span></div><div className="dev-message-list">{activeChat.messages.map((message) => <div className={`dev-message ${message.role}`} key={message.id}>{message.role === "lisa" && <span className="mini-avatar"><Sparkles size={13} /></span>}<div><div className="dev-bubble">{message.text}</div><time>{message.time}{message.role === "lisa" && <span className="delivered"><Check size={12} /> Sent by Lisa</span>}</time></div></div>)}{isResponding && <div className="dev-message lisa"><span className="mini-avatar"><Sparkles size={13} /></span><div className="dev-bubble dev-typing"><i /><i /><i /></div></div>}</div></div>
          <div className="dev-composer-area"><div className="quick-replies">{responseSuggestions.map((suggestion) => <button key={suggestion} onClick={() => sendSuggestion(suggestion)}>{suggestion}</button>)}</div><form className="dev-composer" onSubmit={sendMessage}><textarea value={draft} onChange={(event) => setDraft(event.target.value)} placeholder="Write a reply as Lisa..." rows={2} /><div className="composer-tools"><span>Replying as <b>Lisa</b></span><button type="button" onClick={addReply} className="send-as-button"><Sparkles size={14} /> Use suggested reply</button><button className="dev-send" type="submit" aria-label="Send reply"><ArrowUp size={18} /></button></div></form></div>
        </section>

        <aside className="dev-context"><div className="context-heading"><p className="dev-eyebrow">Conversation context</p><button className="dev-icon-button" aria-label="More context options"><MoreHorizontal size={18} /></button></div><div className="context-profile"><span className="context-avatar">{activeChat.initials}</span><h3>{activeChat.name}</h3><p>Member since August 2025</p><span className="member-status"><span className="live-dot" /> {activeChat.status === "online" ? "Online" : "Away recently"}</span></div><div className="context-section"><div className="context-label"><span>Notes</span><button aria-label="Edit notes">Edit</button></div><p className="note-card">Responds well to gentle questions and short, reassuring messages.</p></div><div className="context-section"><div className="context-label"><span>Conversation tags</span><button aria-label="Add tag">+</button></div><div className="context-tags"><span>wellbeing</span><span>work</span><span>check-in</span></div></div><div className="context-section"><div className="context-label"><span>Safety signals</span><ShieldCheck size={16} /></div><div className="safety-row"><span className="safety-icon"><Check size={14} /></span><span>No active concerns</span><small>Updated now</small></div></div><div className="context-footer"><button><Archive size={15} /> Archive conversation</button><button className="danger"><Flag size={15} /> Report issue</button></div></aside>
      </div>
    </main>
  );
}

const channelName = "lisa-conversation-events";
const dirtyStorageKey = "lisa-conversation-dirty";

type ConversationEvent = { conversationId: string; timestamp: number };

export function notifyConversationChanged(conversationId: string) {
  if (typeof window === "undefined") return;
  const event: ConversationEvent = { conversationId, timestamp: Date.now() };
  if (typeof BroadcastChannel !== "undefined") {
    const channel = new BroadcastChannel(channelName);
    channel.postMessage(event);
    channel.close();
  }
  window.localStorage.setItem(dirtyStorageKey, JSON.stringify(event));
}

export function listenForConversationChanges(callback: (conversationId: string) => void) {
  if (typeof window === "undefined") return () => undefined;
  const channel = typeof BroadcastChannel !== "undefined" ? new BroadcastChannel(channelName) : null;
  const handleEvent = (event: MessageEvent<ConversationEvent>) => {
    if (event.data?.conversationId) callback(event.data.conversationId);
  };
  const handleStorage = (event: StorageEvent) => {
    if (event.key !== dirtyStorageKey || !event.newValue) return;
    const update = JSON.parse(event.newValue) as ConversationEvent;
    callback(update.conversationId);
  };
  channel?.addEventListener("message", handleEvent);
  window.addEventListener("storage", handleStorage);
  return () => {
    channel?.removeEventListener("message", handleEvent);
    channel?.close();
    window.removeEventListener("storage", handleStorage);
  };
}

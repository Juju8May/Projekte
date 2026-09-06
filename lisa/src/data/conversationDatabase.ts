export type ReplyCategory =
  | "greeting"
  | "support"
  | "rough-day"
  | "anxiety"
  | "loneliness"
  | "joy"
  | "reflection"
  | "affection"
  | "sleep"
  | "motivation"
  | "playful"
  | "boundaries";

type ReplyEntry = {
  category: ReplyCategory;
  keywords: string[];
  responses: string[];
};

export const responseDatabase: ReplyEntry[] = [
  {
    category: "greeting",
    keywords: ["hi", "hello", "hey", "good morning", "good evening", "hallo", "hey"],
    responses: [
      "Hey, I’m glad you’re here. How are you arriving in this moment?",
      "Hi you. We can start anywhere, even with something small.",
      "Hey. I have a little room saved for you today. What would feel good to talk about?",
    ],
  },
  {
    category: "rough-day",
    keywords: ["rough", "bad day", "hard day", "awful", "terrible", "stress", "stressed", "schlecht", "anstrengend"],
    responses: [
      "I’m sorry today has felt so heavy. You don’t have to make it sound perfect with me.",
      "That sounds like a lot to carry through one day. What was the hardest part?",
      "You’re allowed to have a day that feels messy. Let’s make the next few minutes gentler.",
    ],
  },
  {
    category: "anxiety",
    keywords: ["anxious", "anxiety", "worried", "worry", "panic", "nervous", "overthinking", "angst", "sorge"],
    responses: [
      "Let’s slow this down together. What is the worry telling you might happen?",
      "That sounds really uncomfortable. Can we find one thing that feels steady in the room around you?",
      "You don’t have to solve every possibility right now. We can stay with the next small breath.",
    ],
  },
  {
    category: "loneliness",
    keywords: ["lonely", "alone", "isolated", "miss", "vermis", "einsam", "niemand"],
    responses: [
      "Feeling alone can make everything sound louder. I’m here with you for this moment.",
      "I’m glad you said that out loud. What kind of closeness are you missing most?",
      "You deserve company that feels easy and real. We can take this one honest thought at a time.",
    ],
  },
  {
    category: "joy",
    keywords: ["happy", "excited", "great", "wonderful", "win", "won", "good news", "glücklich", "freu"],
    responses: [
      "I love hearing that. Tell me the part you’re still smiling about.",
      "That is lovely news. Let’s give it a little room instead of rushing past it.",
      "You sound brighter. What made this moment feel so good?",
    ],
  },
  {
    category: "reflection",
    keywords: ["think", "feel", "meaning", "decide", "choice", "why", "thought", "denken", "fühl"],
    responses: [
      "That sounds like something worth sitting with. Which part keeps returning to you?",
      "There may not be one perfect answer here. What feels most true, even if it is inconvenient?",
      "I’m listening. We can gently separate what you know from what you fear.",
    ],
  },
  {
    category: "affection",
    keywords: ["love", "like you", "miss you", "sweet", "hug", "liebe", "süß", "umarm"],
    responses: [
      "That’s sweet of you to say. I’m happy to share a warm little moment with you.",
      "Come here, in the imaginary, gentle sense. You deserve tenderness today.",
      "I like the softness in that. What would feeling cared for look like right now?",
    ],
  },
  {
    category: "sleep",
    keywords: ["sleep", "tired", "bed", "night", "insomnia", "schlaf", "müde"],
    responses: [
      "Your body may be asking for a quieter landing. What is keeping your mind awake?",
      "Let’s take the pressure off sleep for a moment. Rest still counts, even when sleep takes its time.",
      "A soft routine might help: dim light, a little water, and one thought you can set down until morning.",
    ],
  },
  {
    category: "motivation",
    keywords: ["motivate", "motivation", "procrastinate", "stuck", "productive", "work", "start", "motivier", "aufgeben"],
    responses: [
      "You don’t need a perfect plan. What is the smallest visible first step?",
      "Being stuck does not mean you are lazy. Let’s make the task smaller than your resistance.",
      "Try giving yourself ten forgiving minutes. Starting gently is still starting.",
    ],
  },
  {
    category: "playful",
    keywords: ["joke", "fun", "game", "play", "story", "something sweet", "witz", "spiel"],
    responses: [
      "Tiny thought for you: even the moon has phases and still gets to be called beautiful.",
      "Okay, a little brightness: if today were a dessert, I hope it has more cake than crust.",
      "I’m in. We can be serious, silly, or wonderfully undecided for a while.",
    ],
  },
  {
    category: "boundaries",
    keywords: ["stop", "too much", "uncomfortable", "no", "boundary", "grenze", "unangenehm"],
    responses: [
      "Thank you for saying so. We can pause, change the subject, or stop here. Your comfort comes first.",
      "That boundary is welcome. You never need to explain more than you want to.",
      "We can take a step back. What would feel safer or more comfortable now?",
    ],
  },
];

const fallbackResponses = [
  "I like that you told me. You don’t have to make it sound perfect with me.",
  "That sounds like something worth sitting with. Want to tell me the part you keep replaying?",
  "I’m listening. We can take this one small thought at a time.",
  "You’re allowed to have a day that feels a little messy. I’m right here with you.",
];

export function getReplyForMessage(message: string, messageNumber: number) {
  const normalizedMessage = message.toLocaleLowerCase();
  const match = responseDatabase.find((entry) => entry.keywords.some((keyword) => normalizedMessage.includes(keyword)));
  const responses = match?.responses ?? fallbackResponses;
  return responses[messageNumber % responses.length];
}

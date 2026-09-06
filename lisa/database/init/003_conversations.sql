CREATE TABLE conversations (
  id TEXT PRIMARY KEY,
  name TEXT NOT NULL,
  initials TEXT NOT NULL,
  status TEXT NOT NULL CHECK (status IN ('online', 'away', 'offline')),
  topic TEXT NOT NULL,
  flagged BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE messages (
  id BIGSERIAL PRIMARY KEY,
  conversation_id TEXT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
  role TEXT NOT NULL CHECK (role IN ('user', 'lisa')),
  text TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  read_at TIMESTAMPTZ
);

CREATE INDEX messages_conversation_created_idx ON messages (conversation_id, created_at, id);

INSERT INTO conversations (id, name, initials, status, topic, flagged) VALUES
  ('maya', 'Maya R.', 'MR', 'online', 'A little check-in', FALSE),
  ('jonah', 'Jonah P.', 'JP', 'away', 'Getting to know you', TRUE),
  ('lena', 'Lena K.', 'LK', 'offline', 'A softer morning', FALSE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO messages (conversation_id, role, text, created_at) VALUES
  ('maya', 'user', 'I had a rough day at work and I cannot switch off.', NOW() - INTERVAL '12 minutes'),
  ('maya', 'lisa', 'That sounds like a lot to carry home with you. What part is still looping in your head?', NOW() - INTERVAL '11 minutes'),
  ('maya', 'user', 'The feeling that I should have done more.', NOW() - INTERVAL '10 minutes'),
  ('jonah', 'user', 'Do you remember what I told you yesterday?', NOW() - INTERVAL '20 minutes'),
  ('jonah', 'lisa', 'I remember what we have in this conversation. You were thinking about making more room for your music.', NOW() - INTERVAL '19 minutes'),
  ('lena', 'user', 'I finally slept through the night.', NOW() - INTERVAL '1 day'),
  ('lena', 'lisa', 'I am really glad your body got that rest. How does the day feel from there?', NOW() - INTERVAL '1 day')
ON CONFLICT DO NOTHING;

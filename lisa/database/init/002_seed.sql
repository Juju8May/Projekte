INSERT INTO response_categories (id, name) VALUES
  ('greeting', 'Greeting'),
  ('support', 'Support'),
  ('rough-day', 'Rough day'),
  ('anxiety', 'Anxiety'),
  ('loneliness', 'Loneliness'),
  ('joy', 'Joy'),
  ('reflection', 'Reflection'),
  ('affection', 'Affection'),
  ('sleep', 'Sleep'),
  ('motivation', 'Motivation'),
  ('playful', 'Playful'),
  ('boundaries', 'Boundaries'),
  ('calm', 'Calm'),
  ('nature', 'Nature'),
  ('cozy', 'Cozy'),
  ('bright', 'Bright'),
  ('night', 'Night')
ON CONFLICT (id) DO NOTHING;

INSERT INTO responses (category_id, text, sort_order) VALUES
  ('greeting', 'Hey, I’m glad you’re here. How are you arriving in this moment?', 1),
  ('greeting', 'Hi you. We can start anywhere, even with something small.', 2),
  ('support', 'I’m listening. We can take this one small thought at a time.', 1),
  ('rough-day', 'I’m sorry today has felt so heavy. You don’t have to make it sound perfect with me.', 1),
  ('rough-day', 'That sounds like a lot to carry through one day. What was the hardest part?', 2),
  ('anxiety', 'Let’s slow this down together. What is the worry telling you might happen?', 1),
  ('anxiety', 'You don’t have to solve every possibility right now. We can stay with the next small breath.', 2),
  ('loneliness', 'Feeling alone can make everything sound louder. I’m here with you for this moment.', 1),
  ('joy', 'I love hearing that. Tell me the part you’re still smiling about.', 1),
  ('reflection', 'That sounds like something worth sitting with. Which part keeps returning to you?', 1),
  ('affection', 'That’s sweet of you to say. I’m happy to share a warm little moment with you.', 1),
  ('sleep', 'Let’s take the pressure off sleep for a moment. Rest still counts, even when sleep takes its time.', 1),
  ('motivation', 'You don’t need a perfect plan. What is the smallest visible first step?', 1),
  ('playful', 'I’m in. We can be serious, silly, or wonderfully undecided for a while.', 1),
  ('boundaries', 'Thank you for saying so. We can pause, change the subject, or stop here. Your comfort comes first.', 1)
ON CONFLICT (category_id, text) DO NOTHING;

INSERT INTO images (id, category_id, title, alt_text, asset_path, sort_order) VALUES
  ('bedroom-sleep-clothes', 'calm', 'A quiet bedroom', 'A quiet bedroom with sleep clothes', 'src/data/images/bedroomwithsleepclothes.jpg', 1),
  ('bedroom-underwear', 'cozy', 'Getting ready', 'A bedroom with clothes prepared for the day', 'src/data/images/bedroomwithunderwear.jpg', 2),
  ('bedroom', 'cozy', 'A soft room', 'A softly lit bedroom', 'src/data/images/bedroom.jpg', 3),
  ('city-walk', 'nature', 'A city walk', 'A walk through the city', 'src/data/images/citywalk.jpg', 4),
  ('sunset', 'night', 'Sunset', 'The sky at sunset', 'src/data/images/sunset.jpg', 5),
  ('flowers', 'nature', 'Flowers', 'A group of flowers', 'src/data/images/flowers.jpg', 6),
  ('bedroom-underwear-sexy', 'cozy', 'An intimate evening', 'An intimate bedroom scene', 'src/data/images/bedrromwithunderwearsexy.jpg', 7),
  ('cleaning', 'cozy', 'A fresh room', 'Lisa tidying a bright living room', 'src/data/images/cleaning.jpg', 8),
  ('cooking', 'cozy', 'Cooking together', 'Lisa preparing vegetables in a warm kitchen', 'src/data/images/cooking.jpg', 9)
ON CONFLICT (id) DO NOTHING;
CREATE TABLE response_categories (
  id TEXT PRIMARY KEY,
  name TEXT NOT NULL UNIQUE
);

CREATE TABLE responses (
  id BIGSERIAL PRIMARY KEY,
  category_id TEXT NOT NULL REFERENCES response_categories(id),
  text TEXT NOT NULL,
  sort_order INTEGER NOT NULL DEFAULT 0,
  UNIQUE (category_id, text)
);

CREATE TABLE response_keywords (
  response_id BIGINT NOT NULL REFERENCES responses(id) ON DELETE CASCADE,
  keyword TEXT NOT NULL,
  PRIMARY KEY (response_id, keyword)
);

CREATE TABLE images (
  id TEXT PRIMARY KEY,
  category_id TEXT NOT NULL REFERENCES response_categories(id),
  title TEXT NOT NULL,
  alt_text TEXT NOT NULL,
  asset_path TEXT NOT NULL UNIQUE,
  sort_order INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX response_keywords_keyword_idx ON response_keywords (keyword);
CREATE INDEX images_category_idx ON images (category_id);
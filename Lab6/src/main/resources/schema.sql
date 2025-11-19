-- PostgreSQL schema for Topic and Post.
-- A sequence-backed surrogate primary key is kept on topics (pk BIGSERIAL) to
-- satisfy the autoincrement/sequence requirement; business logic uses the UUID
-- column `id`, which remains unique and is used in foreign keys.

DROP TABLE IF EXISTS posts;
DROP TABLE IF EXISTS topics;

CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

CREATE TABLE IF NOT EXISTS topics (
    pk BIGSERIAL PRIMARY KEY,
    id UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    author VARCHAR(100) NOT NULL,
    view_count INTEGER NOT NULL DEFAULT 0,
    reply_count INTEGER NOT NULL DEFAULT 0,
    pinned BOOLEAN NOT NULL DEFAULT FALSE,
    closed BOOLEAN NOT NULL DEFAULT FALSE,
    tags TEXT[],
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    last_post_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_topics_author ON topics (author);
CREATE INDEX IF NOT EXISTS idx_topics_deleted ON topics (deleted);
CREATE INDEX IF NOT EXISTS idx_topics_pinned ON topics (pinned);

CREATE TABLE IF NOT EXISTS posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id UUID NOT NULL,
    topic_id UUID REFERENCES topics(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    likes INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_posts_topic ON posts (topic_id);
CREATE INDEX IF NOT EXISTS idx_posts_author ON posts (author_id);
CREATE INDEX IF NOT EXISTS idx_posts_title_trgm ON posts USING gin (title gin_trgm_ops);

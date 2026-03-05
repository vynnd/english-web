-- ============================================================
-- English Learning Web App — Database Schema v1
-- Database: PostgreSQL 16+  |  Date: 2026-03-03
--
-- Scalability design decisions:
--   1. UUID PKs — safe for future horizontal sharding
--   2. reading_sessions, srs_reviews, user_points_log use
--      RANGE partitioning by date; add a new partition each year
--   3. user_id is denormalized into high-volume tables so hot
--      queries are index-only and never require cross-table joins
--   4. JSONB for variable-shape data (definitions, collocations,
--      reward configs, weekly word selections)
--   5. Partial indexes on active subsets (e.g. non-MASTERED words)
--   6. leaderboard_snapshots rebuilt periodically; never queried
--      live against the users table
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ─────────────────────────────────────────────────────────────
-- ENUMS
-- ─────────────────────────────────────────────────────────────

CREATE TYPE memory_state  AS ENUM ('NEW', 'LEARNING', 'REVIEW', 'MASTERED', 'RELEARNING');
CREATE TYPE srs_grade     AS ENUM ('EASY', 'GOOD', 'HARD', 'AGAIN');
CREATE TYPE srs_phase     AS ENUM ('LEARNING', 'REVIEW');
CREATE TYPE lang_level    AS ENUM ('A1', 'A2', 'B1', 'B2', 'C1', 'C2');
CREATE TYPE user_tier     AS ENUM ('NONE', 'BEGINNER', 'TRYING', 'EFFORT', 'PERSISTENT', 'ACHIEVEMENT');
CREATE TYPE task_type     AS ENUM (
    'FLASHCARD', 'WRITE_SENTENCE', 'CLOZE_TEST',
    'MULTIPLE_CHOICE', 'COLLOCATION', 'WRITE_PARAGRAPH', 'REACTIVATION'
);
CREATE TYPE reward_src    AS ENUM ('DAILY_SPIN', 'WEEKLY_DICE');
CREATE TYPE reward_kind   AS ENUM ('POINTS', 'BADGE', 'EXTRA_WORD_SLOT', 'PREMIUM_DAY');
CREATE TYPE points_reason AS ENUM ('WORD_EASY', 'APP_TASK_COMPLETE', 'REWARD_REDEEM');

-- ─────────────────────────────────────────────────────────────
-- USERS & SETTINGS
-- ─────────────────────────────────────────────────────────────

CREATE TABLE users (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    email            VARCHAR(255) UNIQUE NOT NULL,
    username         VARCHAR(100) UNIQUE NOT NULL,
    password_hash    VARCHAR(255) NOT NULL,
    is_premium       BOOLEAN      NOT NULL DEFAULT FALSE,
    daily_word_limit INT          NOT NULL DEFAULT 5,   -- raised for premium
    total_points     INT          NOT NULL DEFAULT 0,
    tier             user_tier    NOT NULL DEFAULT 'NONE',
    current_streak   INT          NOT NULL DEFAULT 0,
    longest_streak   INT          NOT NULL DEFAULT 0,
    role             VARCHAR(20)  NOT NULL DEFAULT 'USER',  -- 'USER' | 'ADMIN'
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Versioned daily goals; most recent effective_from wins per user
CREATE TABLE user_goals (
    id                   UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id              UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reading_minutes_goal INT         NOT NULL DEFAULT 10,
    vocab_count_goal     INT         NOT NULL DEFAULT 5,
    effective_from       DATE        NOT NULL,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_user_goals_lookup ON user_goals (user_id, effective_from DESC);

-- ─────────────────────────────────────────────────────────────
-- CONTENT
-- ─────────────────────────────────────────────────────────────

CREATE TABLE topics (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(100) NOT NULL,
    slug          VARCHAR(100) UNIQUE NOT NULL,
    description   TEXT,
    display_order INT          NOT NULL DEFAULT 0,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE articles (
    id                     UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    topic_id               UUID        REFERENCES topics(id) ON DELETE SET NULL,
    title                  VARCHAR(500) NOT NULL,
    content                TEXT         NOT NULL,
    word_count             INT,
    estimated_read_seconds INT,
    language_level         lang_level,
    source_url             VARCHAR(1000),
    is_active              BOOLEAN      NOT NULL DEFAULT TRUE,
    published_at           TIMESTAMPTZ,
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_articles_topic     ON articles (topic_id);
CREATE INDEX idx_articles_level     ON articles (language_level);
CREATE INDEX idx_articles_published ON articles (published_at DESC) WHERE is_active = TRUE;

-- Global word dictionary (shared; not per-user)
CREATE TABLE words (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    word           VARCHAR(200) UNIQUE NOT NULL,
    phonetic       VARCHAR(200),
    audio_url      VARCHAR(500),
    part_of_speech VARCHAR(50),
    vn_meaning     TEXT,
    -- [{lang: "en", meaning: "..."}]
    definitions    JSONB        NOT NULL DEFAULT '[]',
    -- [{sentence: "...", translation: "..."}]
    examples       JSONB        NOT NULL DEFAULT '[]',
    -- ["go on a trip", "take a break", ...]
    collocations   JSONB        NOT NULL DEFAULT '[]',
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_words_word        ON words (word);
CREATE INDEX idx_words_definitions ON words USING GIN (definitions);

-- Which words appear in which articles
CREATE TABLE article_words (
    article_id UUID NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    word_id    UUID NOT NULL REFERENCES words(id)    ON DELETE CASCADE,
    PRIMARY KEY (article_id, word_id)
);
CREATE INDEX idx_article_words_word ON article_words (word_id);

-- ─────────────────────────────────────────────────────────────
-- READING SESSIONS  (partitioned by started_at)
-- ─────────────────────────────────────────────────────────────

CREATE TABLE reading_sessions (
    id               UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id          UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    article_id       UUID        NOT NULL REFERENCES articles(id),
    started_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ended_at         TIMESTAMPTZ,
    duration_seconds INT,
    goal_achieved    BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id, started_at)   -- composite PK required for partitioned table
) PARTITION BY RANGE (started_at);

CREATE TABLE reading_sessions_2026
    PARTITION OF reading_sessions FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');
-- Add reading_sessions_2027, reading_sessions_2028, ... each year

CREATE INDEX idx_rs_user    ON reading_sessions (user_id, started_at DESC);
CREATE INDEX idx_rs_article ON reading_sessions (article_id);

-- Words clicked during a reading session; drives the save-prompt popup
CREATE TABLE word_click_events (
    id                 UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id            UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    article_id         UUID        NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    word_id            UUID        NOT NULL REFERENCES words(id),
    reading_session_id UUID,       -- set after session exists; no FK to avoid partition issues
    clicked_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_wce_session     ON word_click_events (reading_session_id);
CREATE INDEX idx_wce_user_recent ON word_click_events (user_id, clicked_at DESC);

-- ─────────────────────────────────────────────────────────────
-- VOCABULARY LEARNING — SRS
-- ─────────────────────────────────────────────────────────────

-- One row per user×word; tracks both SRS state and application level
CREATE TABLE user_vocabulary (
    id                      UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    word_id                 UUID         NOT NULL REFERENCES words(id),

    -- SRS memory fields
    memory_state            memory_state NOT NULL DEFAULT 'NEW',
    srs_interval_minutes    INT          NOT NULL DEFAULT 0,
    srs_due_at              TIMESTAMPTZ,
    review_count            INT          NOT NULL DEFAULT 0,
    last_reviewed_at        TIMESTAMPTZ,

    -- Vocabulary application assessment (independent from SRS)
    -- 0 = locked (word not yet EASY); 1–6 = active level
    application_level       INT          NOT NULL DEFAULT 0
                                         CHECK (application_level BETWEEN 0 AND 6),
    application_unlocked_at TIMESTAMPTZ,

    saved_at                TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    source_article_id       UUID         REFERENCES articles(id) ON DELETE SET NULL,

    UNIQUE (user_id, word_id)
);
CREATE INDEX idx_uv_user    ON user_vocabulary (user_id);
-- Hot path: fetch words due for review
CREATE INDEX idx_uv_due     ON user_vocabulary (user_id, srs_due_at)
    WHERE memory_state <> 'MASTERED';
CREATE INDEX idx_uv_state   ON user_vocabulary (user_id, memory_state);
CREATE INDEX idx_uv_applv   ON user_vocabulary (user_id, application_level);

-- Immutable review log (partitioned by reviewed_at)
CREATE TABLE srs_reviews (
    id                        UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_vocabulary_id        UUID         NOT NULL REFERENCES user_vocabulary(id) ON DELETE CASCADE,
    user_id                   UUID         NOT NULL, -- denormalized for partition pruning
    response_time_ms          INT          NOT NULL,
    grade                     srs_grade    NOT NULL,
    phase                     srs_phase    NOT NULL,
    previous_interval_minutes INT,
    new_interval_minutes      INT,
    reviewed_at               TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id, reviewed_at)
) PARTITION BY RANGE (reviewed_at);

CREATE TABLE srs_reviews_2026
    PARTITION OF srs_reviews FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');

CREATE INDEX idx_srr_uv   ON srs_reviews (user_vocabulary_id);
CREATE INDEX idx_srr_user ON srs_reviews (user_id, reviewed_at DESC);

-- ─────────────────────────────────────────────────────────────
-- DAILY PROGRESS & STREAKS
-- ─────────────────────────────────────────────────────────────

-- One row per user per calendar day; all three legs must be TRUE for streak
CREATE TABLE daily_progress (
    id                    UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id               UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date                  DATE        NOT NULL,

    -- Leg 1: reading
    reading_seconds       INT         NOT NULL DEFAULT 0,
    reading_goal_achieved BOOLEAN     NOT NULL DEFAULT FALSE,

    -- Leg 2: vocabulary learning
    words_saved           INT         NOT NULL DEFAULT 0,
    words_reached_easy    INT         NOT NULL DEFAULT 0,
    vocab_goal_achieved   BOOLEAN     NOT NULL DEFAULT FALSE,

    -- Leg 3: vocabulary review (≥ 50% of vocab goal)
    reviews_completed     INT         NOT NULL DEFAULT 0,
    review_goal_achieved  BOOLEAN     NOT NULL DEFAULT FALSE,

    -- Streak snapshot for this day
    streak_maintained     BOOLEAN     NOT NULL DEFAULT FALSE,
    streak_count          INT         NOT NULL DEFAULT 0,

    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    UNIQUE (user_id, date)
);
CREATE INDEX idx_dp_user ON daily_progress (user_id, date DESC);

-- ─────────────────────────────────────────────────────────────
-- VOCABULARY APPLICATION ASSESSMENT  (6-level system)
-- ─────────────────────────────────────────────────────────────

-- Each row = one task attempt at a specific level for a saved word
CREATE TABLE application_tasks (
    id                   UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_vocabulary_id   UUID        NOT NULL REFERENCES user_vocabulary(id) ON DELETE CASCADE,
    user_id              UUID        NOT NULL,  -- denormalized
    level                INT         NOT NULL CHECK (level BETWEEN 1 AND 6),
    task_type            task_type   NOT NULL,
    prompt               TEXT,
    user_response        TEXT,
    is_correct           BOOLEAN,
    -- LV4+ AI evaluation (0–100); NULL when not applicable
    ai_naturalness_score FLOAT       CHECK (ai_naturalness_score BETWEEN 0 AND 100),
    -- LV5: flags unnatural / forced usage of word
    is_forced_usage      BOOLEAN,
    response_time_ms     INT,
    -- ISO Monday of the week this task belongs to (for weekly grouping)
    week_reference       DATE,
    completed_at         TIMESTAMPTZ,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_at_uv   ON application_tasks (user_vocabulary_id);
CREATE INDEX idx_at_user ON application_tasks (user_id, completed_at DESC);
CREATE INDEX idx_at_week ON application_tasks (user_id, week_reference);

-- ─────────────────────────────────────────────────────────────
-- WEEKLY REVIEW SETS
-- ─────────────────────────────────────────────────────────────

-- System-generated 6-word selection per user per week
CREATE TABLE weekly_review_sets (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    week_start     DATE        NOT NULL, -- ISO Monday
    -- [{user_vocabulary_id, word_id,
    --   selection_reason: NEAR_LEVELUP|NEAR_DUE|HIGHEST_LEVEL|UNUSED}]
    selected_words JSONB       NOT NULL DEFAULT '[]',
    -- {description, value} — announced at start of week
    fixed_reward   JSONB,
    is_completed   BOOLEAN     NOT NULL DEFAULT FALSE,
    completed_at   TIMESTAMPTZ,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, week_start)
);
CREATE INDEX idx_wrs_user ON weekly_review_sets (user_id, week_start DESC);

-- Individual tasks within a weekly review set
CREATE TABLE weekly_review_tasks (
    id                   UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    weekly_review_set_id UUID        NOT NULL REFERENCES weekly_review_sets(id) ON DELETE CASCADE,
    user_vocabulary_id   UUID        NOT NULL REFERENCES user_vocabulary(id),
    application_task_id  UUID        REFERENCES application_tasks(id),
    is_completed         BOOLEAN     NOT NULL DEFAULT FALSE,
    completed_at         TIMESTAMPTZ
);
CREATE INDEX idx_wrt_set ON weekly_review_tasks (weekly_review_set_id);
CREATE INDEX idx_wrt_uv  ON weekly_review_tasks (user_vocabulary_id);

-- ─────────────────────────────────────────────────────────────
-- MISSIONS
-- ─────────────────────────────────────────────────────────────

CREATE TABLE daily_missions (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date                DATE        NOT NULL,
    easy_words_target   INT         NOT NULL DEFAULT 5,
    easy_words_achieved INT         NOT NULL DEFAULT 0,
    app_review_target   INT         NOT NULL DEFAULT 1,
    app_review_achieved INT         NOT NULL DEFAULT 0,
    is_completed        BOOLEAN     NOT NULL DEFAULT FALSE,
    spin_reward_id      UUID,       -- FK added below after rewards table
    completed_at        TIMESTAMPTZ,
    UNIQUE (user_id, date)
);
CREATE INDEX idx_dm_user ON daily_missions (user_id, date DESC);

CREATE TABLE weekly_missions (
    id                   UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id              UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    week_start           DATE        NOT NULL, -- ISO Monday
    easy_words_target    INT         NOT NULL DEFAULT 20,
    easy_words_achieved  INT         NOT NULL DEFAULT 0,
    app_review_target    INT         NOT NULL DEFAULT 6,
    app_review_achieved  INT         NOT NULL DEFAULT 0,
    is_completed         BOOLEAN     NOT NULL DEFAULT FALSE,
    dice_count           INT,        -- number of dice rolled on completion
    dice_reward_id       UUID,       -- FK added below after rewards table
    completed_at         TIMESTAMPTZ,
    UNIQUE (user_id, week_start)
);
CREATE INDEX idx_wm_user ON weekly_missions (user_id, week_start DESC);

-- ─────────────────────────────────────────────────────────────
-- REWARDS
-- ─────────────────────────────────────────────────────────────

-- Prize templates for spin/dice
CREATE TABLE reward_catalog (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name             VARCHAR(200) NOT NULL,
    description      TEXT,
    reward_type      reward_kind  NOT NULL,
    value            JSONB        NOT NULL, -- {amount: 100} | {days: 3} etc.
    -- Probability weight for random daily spin (0–1, values should sum to 1)
    spin_probability FLOAT,
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Actual reward instances granted to users
CREATE TABLE rewards (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    catalog_id    UUID        REFERENCES reward_catalog(id),
    reward_source reward_src  NOT NULL,
    -- For weekly dice: dice_count × fixed reward value
    dice_count    INT,
    earned_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    redeemed_at   TIMESTAMPTZ
);
CREATE INDEX idx_rewards_user ON rewards (user_id, earned_at DESC);

-- Back-fill deferred FKs for missions → rewards
ALTER TABLE daily_missions
    ADD CONSTRAINT fk_dm_spin_reward
    FOREIGN KEY (spin_reward_id) REFERENCES rewards(id) ON DELETE SET NULL;

ALTER TABLE weekly_missions
    ADD CONSTRAINT fk_wm_dice_reward
    FOREIGN KEY (dice_reward_id) REFERENCES rewards(id) ON DELETE SET NULL;

-- ─────────────────────────────────────────────────────────────
-- POINTS & LEADERBOARD
-- ─────────────────────────────────────────────────────────────

-- Immutable event log; source of truth for all point changes
-- Partitioned by created_at for archival / cold-storage of old data
CREATE TABLE user_points_log (
    id           UUID          NOT NULL DEFAULT gen_random_uuid(),
    user_id      UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    points_delta INT           NOT NULL,   -- positive = earned, negative = spent
    reason       points_reason NOT NULL,
    reference_id UUID,                     -- polymorphic FK (word, task, reward, …)
    total_after  INT           NOT NULL,   -- denormalized running total at event time
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

CREATE TABLE user_points_log_2026
    PARTITION OF user_points_log FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');

CREATE INDEX idx_upl_user ON user_points_log (user_id, created_at DESC);
CREATE INDEX idx_upl_ts   ON user_points_log (created_at DESC);

-- Periodic snapshots powering the leaderboard UI
-- Rebuilt nightly (or on-demand) — never queried live against users
CREATE TABLE leaderboard_snapshots (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID        NOT NULL REFERENCES users(id),
    tier         user_tier   NOT NULL,
    points       INT         NOT NULL,
    rank         INT         NOT NULL,
    period_start DATE        NOT NULL,
    period_end   DATE,
    promoted     BOOLEAN     NOT NULL DEFAULT FALSE, -- top-15 promotion flag
    snapshot_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_lb_tier_rank ON leaderboard_snapshots (tier, period_start, rank);
CREATE INDEX idx_lb_user      ON leaderboard_snapshots (user_id, period_start DESC);

-- ─────────────────────────────────────────────────────────────
-- MAINTENANCE HELPERS
-- ─────────────────────────────────────────────────────────────

-- Auto-update updated_at timestamps
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_articles_updated_at
    BEFORE UPDATE ON articles
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_words_updated_at
    BEFORE UPDATE ON words
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_daily_progress_updated_at
    BEFORE UPDATE ON daily_progress
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

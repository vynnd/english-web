# API Specification — English Learning Web App
**Version:** 1.0 | **Base URL:** `/api/v1` | **Auth:** JWT Bearer Token

---

## Common Response Envelope
```json
{ "success": true, "data": { ... }, "error": null }
```
```json
{ "success": false, "data": null, "error": { "code": "ERROR_CODE", "message": "..." } }
```

---

## 1. Auth
| # | Method | Endpoint | Auth | Description |
|---|--------|----------|------|-------------|
| 1.1 | POST | `/auth/register` | No | Register new account |
| 1.2 | POST | `/auth/login` | No | Login, return access + refresh token |
| 1.3 | POST | `/auth/refresh` | No | Refresh access token |
| 1.4 | POST | `/auth/logout` | Yes | Invalidate refresh token |

**1.1 Register** — body: `{ email, username, password }`
**1.2 Login** — body: `{ email, password }` → `{ accessToken, refreshToken, user }`
**1.3 Refresh** — body: `{ refreshToken }` → `{ accessToken }`

---

## 2. Users & Profile
| # | Method | Endpoint | Auth | Description |
|---|--------|----------|------|-------------|
| 2.1 | GET | `/users/me` | Yes | Get current user profile |
| 2.2 | PUT | `/users/me` | Yes | Update profile (username) |
| 2.3 | GET | `/users/me/goals` | Yes | Get current daily goals |
| 2.4 | PUT | `/users/me/goals` | Yes | Update daily goals |

**2.3 Goals response:** `{ readingMinutesGoal, vocabCountGoal, effectiveFrom }`
**2.4 Goals body:** `{ readingMinutesGoal, vocabCountGoal }` — creates new versioned goal row

---

## 3. Topics
| # | Method | Endpoint | Auth | Description |
|---|--------|----------|------|-------------|
| 3.1 | GET | `/topics` | No | List all active topics |

**3.1 Response:** `[{ id, name, slug, description, displayOrder }]`

---

## 4. Articles
| # | Method | Endpoint | Auth | Description |
|---|--------|----------|------|-------------|
| 4.1 | GET | `/articles` | No | List articles (paginated) |
| 4.2 | GET | `/articles/:id` | No | Get full article content |

**4.1 Query params:** `topicId`, `level` (A1–C2), `page`, `size`
**4.1 Response:** `{ content: [{ id, title, topicId, languageLevel, wordCount, estimatedReadSeconds, publishedAt }], page, totalPages }`
**4.2 Response:** full article including `content` text + word list

---

## 5. Word Lookup
| # | Method | Endpoint | Auth | Description |
|---|--------|----------|------|-------------|
| 5.1 | GET | `/words/:word/tooltip` | No | Single-click: pronunciation + meaning |
| 5.2 | GET | `/words/:word/detail` | Yes | Hold: full card with examples, add-to-vocab status |

**5.1 Response:** `{ word, phonetic, partOfSpeech, shortMeaning }`
**5.2 Response:** `{ word, phonetic, partOfSpeech, definitions, examples, collocations, isSavedByUser, userVocabularyId }`

---

## 6. Reading Sessions
| # | Method | Endpoint | Auth | Description |
|---|--------|----------|------|-------------|
| 6.1 | POST | `/reading-sessions` | Yes | Start a reading session |
| 6.2 | PATCH | `/reading-sessions/:id/end` | Yes | End session, record duration |
| 6.3 | POST | `/reading-sessions/:id/word-clicks` | Yes | Track a word click during reading |
| 6.4 | GET | `/reading-sessions/:id/pending-words` | Yes | Get clicked words for save popup |

**6.1 Body:** `{ articleId }`
**6.2 Body:** `{ durationSeconds }` — also updates daily_progress reading leg
**6.3 Body:** `{ wordId }` — inserts into word_click_events
**6.4 Response:** `[{ wordId, word, phonetic, shortMeaning, isAlreadySaved }]` — deduplicated list

---

## 7. Vocabulary (Save & Manage)
| # | Method | Endpoint | Auth | Description |
|---|--------|----------|------|-------------|
| 7.1 | POST | `/vocabulary/save` | Yes | Save selected words after reading popup |
| 7.2 | GET | `/vocabulary` | Yes | List user's vocabulary |
| 7.3 | GET | `/vocabulary/:id` | Yes | Get single vocab entry detail |
| 7.4 | DELETE | `/vocabulary/:id` | Yes | Remove word from vocab list |
| 7.5 | GET | `/vocabulary/due` | Yes | Get words due for SRS review |
| 7.6 | GET | `/vocabulary/daily-limit` | Yes | Check remaining saves for today |

**7.1 Body:** `{ wordIds: [], sourceArticleId }` — enforces daily limit (5 free / more premium); returns `{ saved: [], rejected: { reason: 'DAILY_LIMIT_REACHED', remaining: 0 } }`
**7.2 Query params:** `memoryState`, `applicationLevel`, `page`, `size`
**7.2 Response:** `[{ id, word, phonetic, memoryState, srsInterval, srsDueAt, applicationLevel, savedAt }]`
**7.5 Response:** ordered flashcard queue `[{ userVocabularyId, word, phonetic, definitions, reviewType: 'WORD_TO_MEANING'|'MEANING_TO_WORD' }]`
**7.6 Response:** `{ used: 3, limit: 5, remaining: 2, isPremium: false }`

---

## 8. SRS Reviews
| # | Method | Endpoint | Auth | Description |
|---|--------|----------|------|-------------|
| 8.1 | POST | `/srs/reviews` | Yes | Submit a review result (grade + response time) |

**8.1 Body:** `{ userVocabularyId, responseTimeMs }` — server calculates grade from responseTimeMs, computes next interval, updates memory_state, logs to srs_reviews, updates daily_progress review leg
**8.1 Response:** `{ grade, nextReviewAt, newMemoryState, pointsEarned }`
> **SRS Logic:**
> Grade from response time: <3s=EASY, 3–5s=GOOD, 5–10s=HARD, >10s=AGAIN
> LEARNING phase: EASY→+1d, GOOD→+1h, HARD→+10m, AGAIN→+5m
> REVIEW phase: interval × (EASY=2, GOOD=1.5, HARD=0.7, AGAIN=0.3→reset 10m)
> If word reaches EASY → unlock application assessment (application_level=1), award +50 points

---

## 9. Vocabulary Application Assessment
| # | Method | Endpoint | Auth | Description |
|---|--------|----------|------|-------------|
| 9.1 | GET | `/vocabulary/:id/application/status` | Yes | Get current level + progress toward next level |
| 9.2 | GET | `/vocabulary/:id/application/task` | Yes | Get next task to complete for this word |
| 9.3 | POST | `/vocabulary/:id/application/submit` | Yes | Submit task response |

**9.1 Response:** `{ currentLevel, levelName, taskType, progressToNextLevel: { condition, current, required } }`
**9.2 Response:** `{ taskId, level, taskType, prompt, options? }` — options present for MULTIPLE_CHOICE/CLOZE_TEST
**9.3 Body:** `{ taskId, response, responseTimeMs }` — server evaluates; LV4+ calls AI for naturalness score
**9.3 Response:** `{ isCorrect, score, leveledUp, newLevel, pointsEarned, feedback }`
> +100 points on correct task completion

---

## 10. Weekly Review
| # | Method | Endpoint | Auth | Description |
|---|--------|----------|------|-------------|
| 10.1 | GET | `/weekly-review` | Yes | Get current week's review set (auto-generated if not exists) |
| 10.2 | GET | `/weekly-review/tasks` | Yes | List the 6 tasks in the current weekly set |
| 10.3 | POST | `/weekly-review/tasks/:taskId/complete` | Yes | Mark a weekly task as done |

**10.1 Response:** `{ id, weekStart, selectedWords: [{ word, selectionReason }], fixedReward, isCompleted, completedCount, totalCount }`
> Unlocked only when user has ≥3 LV2+ words; returns `{ locked: true }` otherwise
> Word selection: 2 near-levelup + 2 near-due + 1 highest-level + 1 unused-recently
**10.3 Body:** `{ applicationTaskId }` — links completed application task to weekly task

---

## 11. Daily Progress & Streak
| # | Method | Endpoint | Auth | Description |
|---|--------|----------|------|-------------|
| 11.1 | GET | `/progress/today` | Yes | Get today's progress across all 3 streak legs |
| 11.2 | GET | `/progress/streak` | Yes | Get current streak count + longest streak |
| 11.3 | GET | `/progress/history` | Yes | Get progress history by date range |

**11.1 Response:**
```json
{
  "date": "2026-03-04",
  "reading": { "seconds": 420, "goalSeconds": 600, "achieved": false },
  "vocab": { "wordsReachedEasy": 3, "goal": 5, "achieved": false },
  "review": { "completed": 2, "goal": 3, "achieved": false },
  "streakMaintained": false,
  "streakCount": 7
}
```
**11.3 Query params:** `from` (date), `to` (date)

---

## 12. Daily Missions
| # | Method | Endpoint | Auth | Description |
|---|--------|----------|------|-------------|
| 12.1 | GET | `/missions/daily` | Yes | Get today's daily mission status |
| 12.2 | POST | `/missions/daily/claim` | Yes | Claim spin reward after completing mission |

**12.1 Response:** `{ date, easyWordsTarget, easyWordsAchieved, appReviewTarget, appReviewAchieved, isCompleted, spinClaimed, spinReward? }`
**12.2** — triggers random spin from reward_catalog weighted by spin_probability; returns `{ reward: { name, type, value } }`

---

## 13. Weekly Missions
| # | Method | Endpoint | Auth | Description |
|---|--------|----------|------|-------------|
| 13.1 | GET | `/missions/weekly` | Yes | Get current week's mission status |
| 13.2 | POST | `/missions/weekly/claim` | Yes | Claim dice reward after completing mission |

**13.1 Response:** `{ weekStart, easyWordsTarget, easyWordsAchieved, appReviewTarget, appReviewAchieved, isCompleted, diceCount, fixedReward, diceClaimed, diceReward? }`
**13.2** — rolls dice, multiplies fixed reward; returns `{ diceCount, fixedReward, totalReward }`

---

## 14. Rewards
| # | Method | Endpoint | Auth | Description |
|---|--------|----------|------|-------------|
| 14.1 | GET | `/rewards` | Yes | Get user's reward history |
| 14.2 | GET | `/rewards/catalog` | Yes | List active prizes in reward catalog |

**14.1 Query params:** `page`, `size`
**14.1 Response:** `[{ id, name, type, value, source, earnedAt, redeemedAt }]`
**14.2 Response:** `[{ id, name, description, type, value, spinProbability }]`

---

## 15. Points & Leaderboard
| # | Method | Endpoint | Auth | Description |
|---|--------|----------|------|-------------|
| 15.1 | GET | `/points/history` | Yes | Get user's points log |
| 15.2 | GET | `/leaderboard` | Yes | Get leaderboard for a tier |
| 15.3 | GET | `/leaderboard/me` | Yes | Get current user's rank + tier |

**15.1 Query params:** `page`, `size`
**15.1 Response:** `[{ pointsDelta, reason, totalAfter, createdAt }]`
**15.2 Query params:** `tier` (BEGINNER|TRYING|EFFORT|PERSISTENT|ACHIEVEMENT)
**15.2 Response:** `[{ rank, username, points, promoted }]`
**15.3 Response:** `{ tier, points, rank, pointsToNextTier, promoted }`

---

## Summary

| Domain | Count |
|--------|-------|
| Auth | 4 |
| Users & Profile | 4 |
| Topics | 1 |
| Articles | 2 |
| Word Lookup | 2 |
| Reading Sessions | 4 |
| Vocabulary | 6 |
| SRS Reviews | 1 |
| Application Assessment | 3 |
| Weekly Review | 3 |
| Daily Progress & Streak | 3 |
| Daily Missions | 2 |
| Weekly Missions | 2 |
| Rewards | 2 |
| Points & Leaderboard | 3 |
| **Total** | **42** |

import api from './axios'

export interface ReadingSession {
  sessionId: string
  startedAt: string
}

export interface PendingWord {
  wordId: string
  word: string
  phonetic: string
  partOfSpeech: string
  definitions: string[]
}

export const readingSessionsApi = {
  start: (articleId: string) =>
    api.post<{ success: boolean; data: ReadingSession }>('/reading-sessions', { articleId }),

  end: (sessionId: string, durationSeconds: number) =>
    api.patch<{ success: boolean; data: { sessionId: string; durationSeconds: number; goalAchieved: boolean } }>(
      `/reading-sessions/${sessionId}/end`,
      { durationSeconds }
    ),

  trackWordClick: (sessionId: string, wordId: string) =>
    api.post(`/reading-sessions/${sessionId}/word-clicks`, { wordId }),

  getPendingWords: (sessionId: string) =>
    api.get<{ success: boolean; data: PendingWord[] }>(`/reading-sessions/${sessionId}/pending-words`),
}

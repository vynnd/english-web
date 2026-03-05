import api from './axios'

export interface VocabularyEntry {
  id: string
  wordId: string
  word: string
  phonetic: string
  partOfSpeech: string
  vnMeaning: string
  definitions: string[]
  memoryState: string
  appLevel: number
  savedAt: string
  nextReviewAt: string | null
}

export interface SaveResult {
  saved: string[]
  skipped: string[]
  limitReached?: boolean
}

export interface DueWord {
  userVocabularyId: string
  word: string
  phonetic: string
  partOfSpeech: string
  definitions: string[]
  examples: string[]
}

export const vocabularyApi = {
  getDailyLimit: () =>
    api.get<{ success: boolean; data: { saved: number; limit: number; remaining: number } }>('/vocabulary/daily-limit'),

  save: (wordIds: string[], sourceArticleId?: string) =>
    api.post<{ success: boolean; data: SaveResult }>('/vocabulary/save', { wordIds, sourceArticleId }),

  list: (params: { page?: number; size?: number }) =>
    api.get<{ success: boolean; data: { content: VocabularyEntry[]; page: number; totalPages: number } }>('/vocabulary', { params }),

  get: (id: string) =>
    api.get<{ success: boolean; data: VocabularyEntry }>(`/vocabulary/${id}`),

  delete: (id: string) =>
    api.delete(`/vocabulary/${id}`),

  getDue: () =>
    api.get<{ success: boolean; data: DueWord[] }>('/vocabulary/due'),

  getSavedInArticle: (articleId: string) =>
    api.get<{ success: boolean; data: string[] }>(`/vocabulary/saved-in-article/${articleId}`),
}

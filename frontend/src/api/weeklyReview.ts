import api from './axios'

export interface WeeklyReviewWord {
  userVocabularyId: string
  word: string
  appLevel: number
}

export interface WeeklyReview {
  weekStart: string
  words: WeeklyReviewWord[]
  isUnlocked: boolean
}

export interface WeeklyTask {
  id: string
  taskType: string
  description: string
  isCompleted: boolean
  wordId: string
  word: string
}

export const weeklyReviewApi = {
  getCurrent: () =>
    api.get<{ success: boolean; data: WeeklyReview }>('/weekly-review'),

  getTasks: () =>
    api.get<{ success: boolean; data: WeeklyTask[] }>('/weekly-review/tasks'),

  completeTask: (taskId: string, applicationTaskId?: string) =>
    api.post<{ success: boolean; data: unknown }>(`/weekly-review/tasks/${taskId}/complete`, {
      applicationTaskId: applicationTaskId ?? null,
    }),
}

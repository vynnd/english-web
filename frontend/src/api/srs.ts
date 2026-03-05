import api from './axios'

export interface ReviewResult {
  userVocabularyId: string
  nextReviewAt: string
  srsLevel: number
  intervalDays: number
  pointsEarned: number
}

export const srsApi = {
  submitReview: (userVocabularyId: string, responseTimeMs: number) =>
    api.post<{ success: boolean; data: ReviewResult }>('/srs/reviews', { userVocabularyId, responseTimeMs }),
}

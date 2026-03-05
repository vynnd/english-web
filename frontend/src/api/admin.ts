import api from './axios'

export interface AdminArticle {
  id: string
  title: string
  content: string
  topicId: string | null
  topicName: string | null
  languageLevel: string
  sourceUrl: string
  wordCount: number
  isActive: boolean
  publishedAt: string
}

export const adminApi = {
  getArticles: (params?: { topicId?: string; level?: string; page?: number; size?: number }) =>
    api.get<{ success: boolean; data: { content: AdminArticle[]; page: number; totalPages: number; totalElements: number } }>('/articles', { params }),

  createArticle: (data: { topicId: string; title: string; content: string; level: string; sourceUrl?: string }) =>
    api.post<{ success: boolean; data: AdminArticle }>('/admin/articles', data),

  updateArticle: (id: string, data: { topicId?: string; title?: string; content?: string; level?: string; sourceUrl?: string }) =>
    api.put<{ success: boolean; data: AdminArticle }>(`/admin/articles/${id}`, data),

  deleteArticle: (id: string) =>
    api.delete<{ success: boolean }>(`/admin/articles/${id}`),
}

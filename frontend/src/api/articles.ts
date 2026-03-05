import api from './axios'

export interface Article {
  id: string
  title: string
  topicId: string | null
  languageLevel: string
  wordCount: number
  estimatedReadSeconds: number
  publishedAt: string
}

export interface ArticleDetail extends Article {
  content: string
}

export interface Topic {
  id: string
  name: string
  slug: string
}

export const articlesApi = {
  list: (params: { topicId?: string; level?: string; page?: number; size?: number }) =>
    api.get<{ success: boolean; data: { content: Article[]; page: number; totalPages: number; totalElements: number } }>('/articles', { params }),

  get: (id: string) =>
    api.get<{ success: boolean; data: ArticleDetail }>(`/articles/${id}`),

  getTopics: () =>
    api.get<{ success: boolean; data: Topic[] }>('/topics'),
}

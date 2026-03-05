import api from './axios'

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  user: {
    id: string
    email: string
    username: string
    role: string
    isPremium: boolean
    totalPoints: number
    tier: string
    currentStreak: number
    longestStreak: number
    dailyWordLimit: number
  }
}

export const authApi = {
  register: (email: string, username: string, password: string) =>
    api.post<{ success: boolean; data: AuthResponse }>('/auth/register', { email, username, password }),

  login: (email: string, password: string) =>
    api.post<{ success: boolean; data: AuthResponse }>('/auth/login', { email, password }),

  refresh: (refreshToken: string) =>
    api.post<{ success: boolean; data: { accessToken: string } }>('/auth/refresh', { refreshToken }),

  logout: () => api.post('/auth/logout'),
}

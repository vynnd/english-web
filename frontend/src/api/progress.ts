import api from './axios'

export interface TodayProgress {
  date: string
  reading: { seconds: number; goalSeconds: number; achieved: boolean }
  vocab: { wordsReachedEasy: number; goal: number; achieved: boolean }
  review: { completed: number; goal: number; achieved: boolean }
  streakMaintained: boolean
  streakCount: number
}

export interface StreakInfo {
  currentStreak: number
  longestStreak: number
  lastActiveDate: string | null
}

export const progressApi = {
  getToday: () =>
    api.get<{ success: boolean; data: TodayProgress }>('/progress/today'),

  getStreak: () =>
    api.get<{ success: boolean; data: StreakInfo }>('/progress/streak'),

  getHistory: (from: string, to: string) =>
    api.get<{ success: boolean; data: TodayProgress[] }>('/progress/history', { params: { from, to } }),
}

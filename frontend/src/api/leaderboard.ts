import api from './axios'

export interface LeaderboardEntry {
  rank: number
  userId: string
  username: string
  totalPoints: number
  tier: string
}

export interface MyRank {
  rank: number
  totalPoints: number
  tier: string
  username: string
}

export interface PointsHistoryEntry {
  id: string
  points: number
  source: string
  earnedAt: string
}

export const leaderboardApi = {
  getLeaderboard: (tier: string) =>
    api.get<{ success: boolean; data: LeaderboardEntry[] }>('/leaderboard', { params: { tier } }),

  getMyRank: () =>
    api.get<{ success: boolean; data: MyRank }>('/leaderboard/me'),

  getPointsHistory: (params: { page?: number; size?: number }) =>
    api.get<{ success: boolean; data: { content: PointsHistoryEntry[]; page: number; totalPages: number } }>('/points/history', { params }),
}

import api from './axios'

export interface Reward {
  id: string
  rewardSource: string
  catalog: string | null
  diceCount: number | null
  earnedAt: string
}

export interface RewardCatalog {
  id: string
  name: string
  description: string
  type: string
}

export const rewardsApi = {
  getHistory: (params: { page?: number; size?: number }) =>
    api.get<{ success: boolean; data: { content: Reward[]; page: number; totalPages: number } }>('/rewards', { params }),

  getCatalog: () =>
    api.get<{ success: boolean; data: RewardCatalog[] }>('/rewards/catalog'),
}

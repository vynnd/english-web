import api from './axios'

export interface DailyMission {
  date: string
  easyWordsTarget: number
  easyWordsAchieved: number
  appReviewTarget: number
  appReviewAchieved: number
  isCompleted: boolean
  spinClaimed: boolean
}

export interface WeeklyMission {
  weekStart: string
  easyWordsTarget: number
  easyWordsAchieved: number
  appReviewTarget: number
  appReviewAchieved: number
  isCompleted: boolean
  diceClaimed: boolean
}

export const missionsApi = {
  getDaily: () =>
    api.get<{ success: boolean; data: DailyMission }>('/missions/daily'),

  claimDailySpin: () =>
    api.post<{ success: boolean; data: { rewardId: string; source: string; catalog: string } }>('/missions/daily/claim'),

  getWeekly: () =>
    api.get<{ success: boolean; data: WeeklyMission }>('/missions/weekly'),

  claimWeeklyDice: () =>
    api.post<{ success: boolean; data: { rewardId: string; diceCount: number; source: string } }>('/missions/weekly/claim'),
}

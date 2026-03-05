import api from './axios'

export interface AppStatus {
  currentLevel: number
  levelLabel: string
  tasksCompleted: number
  nextTaskAvailable: boolean
}

export interface AppTask {
  taskId: string
  taskType: string
  prompt: string
  options?: string[]
}

export interface AppSubmitResult {
  correct: boolean
  feedback: string
  newLevel: number
  pointsEarned: number
}

export const applicationApi = {
  getStatus: (uvId: string) =>
    api.get<{ success: boolean; data: AppStatus }>(`/vocabulary/${uvId}/application/status`),

  getNextTask: (uvId: string) =>
    api.get<{ success: boolean; data: AppTask }>(`/vocabulary/${uvId}/application/task`),

  submitTask: (uvId: string, taskId: string, response: string, responseTimeMs: number) =>
    api.post<{ success: boolean; data: AppSubmitResult }>(`/vocabulary/${uvId}/application/submit`, {
      taskId,
      response,
      responseTimeMs,
    }),
}

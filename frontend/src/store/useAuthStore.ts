import { create } from 'zustand'

interface User {
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

interface AuthState {
  user: User | null
  accessToken: string | null
  isAuthenticated: boolean
  isInitializing: boolean
  login: (user: User, accessToken: string, refreshToken: string) => void
  logout: () => void
  setUser: (user: User) => void
  setInitializing: (value: boolean) => void
}

const storedUser = localStorage.getItem('user')
const hasToken = !!localStorage.getItem('accessToken')

export const useAuthStore = create<AuthState>((set) => ({
  user: storedUser ? JSON.parse(storedUser) : null,
  accessToken: localStorage.getItem('accessToken'),
  isAuthenticated: hasToken,
  isInitializing: hasToken && !storedUser,

  login: (user, accessToken, refreshToken) => {
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('refreshToken', refreshToken)
    localStorage.setItem('user', JSON.stringify(user))
    set({ user, accessToken, isAuthenticated: true })
  },

  logout: () => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
    set({ user: null, accessToken: null, isAuthenticated: false })
  },

  setUser: (user) => {
    localStorage.setItem('user', JSON.stringify(user))
    set({ user })
  },
  setInitializing: (value) => set({ isInitializing: value }),
}))

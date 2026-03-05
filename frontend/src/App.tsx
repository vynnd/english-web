import { useEffect } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuthStore } from './store/useAuthStore'
import api from './api/axios'
import { Layout } from './components/layout/Layout'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { HomePage } from './pages/HomePage'
import { ArticlePage } from './pages/ArticlePage'
import { VocabularyPage } from './pages/VocabularyPage'
import { ReviewPage } from './pages/ReviewPage'
import { ApplicationPage } from './pages/ApplicationPage'
import { WeeklyReviewPage } from './pages/WeeklyReviewPage'
import { ProgressPage } from './pages/ProgressPage'
import { MissionsPage } from './pages/MissionsPage'
import { RewardsPage } from './pages/RewardsPage'
import { LeaderboardPage } from './pages/LeaderboardPage'
import { AdminPage } from './pages/AdminPage'

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuthStore()
  if (!isAuthenticated) return <Navigate to="/login" replace />
  return <>{children}</>
}

function AdminRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, user, isInitializing } = useAuthStore()
  if (!isAuthenticated) return <Navigate to="/login" replace />
  if (isInitializing) return null
  if (user?.role !== 'ADMIN') return <Navigate to="/home" replace />
  return <>{children}</>
}

export default function App() {
  const { isAuthenticated, user, setUser, setInitializing } = useAuthStore()

  useEffect(() => {
    if (isAuthenticated && !user) {
      api.get('/users/me')
        .then(res => setUser(res.data.data))
        .catch(() => {})
        .finally(() => setInitializing(false))
    }
  }, [])

  return (
    <Routes>
      <Route path="/" element={<Navigate to={isAuthenticated ? '/home' : '/login'} replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route path="/home" element={<HomePage />} />
        <Route path="/articles/:id" element={<ArticlePage />} />
        <Route path="/vocabulary" element={<VocabularyPage />} />
        <Route path="/review" element={<ReviewPage />} />
        <Route path="/application/:uvId" element={<ApplicationPage />} />
        <Route path="/weekly-review" element={<WeeklyReviewPage />} />
        <Route path="/progress" element={<ProgressPage />} />
        <Route path="/missions" element={<MissionsPage />} />
        <Route path="/rewards" element={<RewardsPage />} />
        <Route path="/leaderboard" element={<LeaderboardPage />} />
        <Route path="/admin" element={<AdminRoute><AdminPage /></AdminRoute>} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

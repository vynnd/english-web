import { Link, useNavigate } from 'react-router-dom'
import { BookOpen, LogOut, User } from 'lucide-react'
import { useAuthStore } from '../../store/useAuthStore'
import { authApi } from '../../api/auth'

export function Navbar() {
  const { user, logout } = useAuthStore()
  const navigate = useNavigate()

  const handleLogout = async () => {
    try { await authApi.logout() } catch {}
    logout()
    navigate('/login')
  }

  return (
    <nav className="sticky top-0 z-40 border-b border-gray-200 bg-white/95 backdrop-blur">
      <div className="mx-auto flex h-14 max-w-7xl items-center justify-between px-4">
        <Link to="/home" className="flex items-center gap-2 font-bold text-blue-600">
          <BookOpen className="h-5 w-5" />
          <span>EnglishWeb</span>
        </Link>
        {user && (
          <div className="flex items-center gap-4">
            <span className="text-sm text-gray-600 font-medium">{user.totalPoints} pts</span>
            <span className="text-sm font-medium text-gray-700 flex items-center gap-1">
              <User className="h-4 w-4" />
              {user.username}
            </span>
            <button
              onClick={handleLogout}
              className="flex items-center gap-1 rounded-lg px-3 py-1.5 text-sm text-gray-500 hover:bg-gray-100 hover:text-gray-700"
            >
              <LogOut className="h-4 w-4" />
              Logout
            </button>
          </div>
        )}
      </div>
    </nav>
  )
}

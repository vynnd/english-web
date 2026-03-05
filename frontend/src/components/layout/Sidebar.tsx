import { NavLink } from 'react-router-dom'
import {
  Home, Library, RotateCcw, Star,
  TrendingUp, Target, Gift, Trophy, ShieldCheck
} from 'lucide-react'
import { useAuthStore } from '../../store/useAuthStore'

const navItems = [
  { to: '/home', icon: Home, label: 'Home' },
  { to: '/vocabulary', icon: Library, label: 'Vocabulary' },
  { to: '/review', icon: RotateCcw, label: 'SRS Review' },
  { to: '/weekly-review', icon: Star, label: 'Weekly Review' },
  { to: '/progress', icon: TrendingUp, label: 'Progress' },
  { to: '/missions', icon: Target, label: 'Missions' },
  { to: '/rewards', icon: Gift, label: 'Rewards' },
  { to: '/leaderboard', icon: Trophy, label: 'Leaderboard' },
]

const linkClass = ({ isActive }: { isActive: boolean }) =>
  `flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
    isActive ? 'bg-blue-50 text-blue-700' : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
  }`

export function Sidebar() {
  const { user } = useAuthStore()

  return (
    <aside className="flex h-full w-56 flex-col border-r border-gray-200 bg-white py-4">
      <nav className="flex flex-col gap-1 px-2">
        {navItems.map(({ to, icon: Icon, label }) => (
          <NavLink key={to} to={to} className={linkClass}>
            <Icon className="h-4 w-4 shrink-0" />
            {label}
          </NavLink>
        ))}

        {user?.role === 'ADMIN' && (
          <>
            <div className="my-2 border-t border-gray-100" />
            <NavLink to="/admin" className={linkClass}>
              <ShieldCheck className="h-4 w-4 shrink-0" />
              Admin
            </NavLink>
          </>
        )}
      </nav>
    </aside>
  )
}

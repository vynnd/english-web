import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Trophy, Crown, ChevronLeft, ChevronRight } from 'lucide-react'
import { leaderboardApi } from '../api/leaderboard'
import { Card, CardBody, CardHeader } from '../components/ui/Card'
import { Badge } from '../components/ui/Badge'
import { Button } from '../components/ui/Button'
import { Spinner } from '../components/ui/Spinner'
import { useAuthStore } from '../store/useAuthStore'

const tiers = ['BEGINNER', 'ELEMENTARY', 'INTERMEDIATE', 'ADVANCED', 'EXPERT']

const tierColors: Record<string, 'gray' | 'blue' | 'green' | 'purple' | 'yellow'> = {
  BEGINNER: 'gray',
  ELEMENTARY: 'blue',
  INTERMEDIATE: 'green',
  ADVANCED: 'purple',
  EXPERT: 'yellow',
}

const medalColors = ['text-yellow-400', 'text-gray-400', 'text-amber-600']

export function LeaderboardPage() {
  const [selectedTier, setSelectedTier] = useState('BEGINNER')
  const [pointsPage, setPointsPage] = useState(0)
  const { user } = useAuthStore()

  const { data: leaderboard, isLoading } = useQuery({
    queryKey: ['leaderboard', selectedTier],
    queryFn: () => leaderboardApi.getLeaderboard(selectedTier).then(r => r.data.data),
  })

  const { data: myRank } = useQuery({
    queryKey: ['my-rank'],
    queryFn: () => leaderboardApi.getMyRank().then(r => r.data.data),
  })

  const { data: pointsHistory } = useQuery({
    queryKey: ['points-history', pointsPage],
    queryFn: () => leaderboardApi.getPointsHistory({ page: pointsPage, size: 10 }).then(r => r.data.data),
  })

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Leaderboard</h1>

      {/* My rank banner */}
      {myRank && (
        <div className="rounded-2xl bg-gradient-to-r from-blue-600 to-violet-600 p-5 text-white">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-blue-100 text-sm">Your ranking</p>
              <p className="text-3xl font-bold">#{myRank.rank}</p>
              <p className="text-blue-200 text-sm">{myRank.totalPoints} points</p>
            </div>
            <div className="text-right">
              <Badge color={tierColors[myRank.tier] ?? 'gray'} className="mb-2">
                {myRank.tier}
              </Badge>
              <p className="text-white font-medium">{user?.username}</p>
            </div>
          </div>
        </div>
      )}

      {/* Tier tabs */}
      <div className="flex gap-2 overflow-x-auto pb-1">
        {tiers.map(tier => (
          <button
            key={tier}
            onClick={() => setSelectedTier(tier)}
            className={`rounded-full px-4 py-1.5 text-sm font-medium whitespace-nowrap transition-colors ${
              selectedTier === tier
                ? 'bg-blue-600 text-white'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            {tier}
          </button>
        ))}
      </div>

      {/* Leaderboard table */}
      <Card>
        <CardHeader>
          <h2 className="font-semibold text-gray-900">{selectedTier} Tier — Top 15</h2>
        </CardHeader>
        <CardBody className="p-0">
          {isLoading ? (
            <div className="flex justify-center py-10"><Spinner /></div>
          ) : (
            <div className="divide-y divide-gray-100">
              {leaderboard?.map((entry, idx) => (
                <div
                  key={entry.userId}
                  className={`flex items-center gap-4 px-5 py-3 ${entry.username === user?.username ? 'bg-blue-50' : ''}`}
                >
                  <div className="w-8 text-center">
                    {idx < 3 ? (
                      <Crown className={`h-5 w-5 mx-auto ${medalColors[idx]}`} />
                    ) : (
                      <span className="text-sm font-semibold text-gray-400">#{entry.rank}</span>
                    )}
                  </div>
                  <div className="flex-1">
                    <p className={`font-medium ${entry.username === user?.username ? 'text-blue-700' : 'text-gray-800'}`}>
                      {entry.username}
                      {entry.username === user?.username && <span className="ml-1.5 text-xs text-blue-500">(you)</span>}
                    </p>
                  </div>
                  <div className="flex items-center gap-1 text-sm font-semibold text-gray-700">
                    <Trophy className="h-4 w-4 text-yellow-400" />
                    {entry.totalPoints.toLocaleString()}
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardBody>
      </Card>

      {/* Points history */}
      {pointsHistory && (
        <Card>
          <CardHeader>
            <h2 className="font-semibold text-gray-900">Points History</h2>
          </CardHeader>
          <CardBody>
            <div className="space-y-2">
              {pointsHistory.content.map(entry => (
                <div key={entry.id} className="flex items-center justify-between py-2 border-b border-gray-100 last:border-0">
                  <div>
                    <p className="text-sm text-gray-700">{entry.source}</p>
                    <p className="text-xs text-gray-400">{new Date(entry.earnedAt).toLocaleDateString()}</p>
                  </div>
                  <span className="text-sm font-semibold text-green-600">+{entry.points}</span>
                </div>
              ))}
            </div>
            {pointsHistory.totalPages > 1 && (
              <div className="flex items-center justify-center gap-2 mt-4">
                <Button variant="ghost" size="sm" onClick={() => setPointsPage(p => p - 1)} disabled={pointsPage === 0}>
                  <ChevronLeft className="h-4 w-4" />
                </Button>
                <span className="text-sm text-gray-600">Page {pointsPage + 1} of {pointsHistory.totalPages}</span>
                <Button variant="ghost" size="sm" onClick={() => setPointsPage(p => p + 1)} disabled={pointsPage >= pointsHistory.totalPages - 1}>
                  <ChevronRight className="h-4 w-4" />
                </Button>
              </div>
            )}
          </CardBody>
        </Card>
      )}
    </div>
  )
}

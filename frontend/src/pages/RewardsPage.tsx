import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Gift, ChevronLeft, ChevronRight } from 'lucide-react'
import { rewardsApi } from '../api/rewards'
import { Card, CardBody, CardHeader } from '../components/ui/Card'
import { Badge } from '../components/ui/Badge'
import { Button } from '../components/ui/Button'
import { Spinner } from '../components/ui/Spinner'

const sourceLabels: Record<string, string> = {
  DAILY_SPIN: 'Daily Spin',
  WEEKLY_DICE: 'Weekly Dice',
}

export function RewardsPage() {
  const [page, setPage] = useState(0)

  const { data: history, isLoading } = useQuery({
    queryKey: ['rewards', page],
    queryFn: () => rewardsApi.getHistory({ page, size: 20 }).then(r => r.data.data),
  })

  const { data: catalog } = useQuery({
    queryKey: ['reward-catalog'],
    queryFn: () => rewardsApi.getCatalog().then(r => r.data.data),
  })

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Rewards</h1>

      {/* Catalog */}
      {catalog && catalog.length > 0 && (
        <Card>
          <CardHeader>
            <h2 className="font-semibold text-gray-900">This Week's Prize Catalog</h2>
          </CardHeader>
          <CardBody>
            <div className="grid gap-3 sm:grid-cols-2">
              {catalog.map(item => (
                <div key={item.id} className="rounded-xl border border-gray-200 p-4">
                  <div className="flex items-start gap-3">
                    <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-yellow-100">
                      <Gift className="h-5 w-5 text-yellow-600" />
                    </div>
                    <div>
                      <p className="font-semibold text-gray-900">{item.name}</p>
                      <p className="text-sm text-gray-500">{item.description}</p>
                      <Badge color="yellow" className="mt-1">{item.type}</Badge>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </CardBody>
        </Card>
      )}

      {/* History */}
      <Card>
        <CardHeader>
          <h2 className="font-semibold text-gray-900">Reward History</h2>
        </CardHeader>
        <CardBody>
          {isLoading ? (
            <div className="flex justify-center py-8"><Spinner /></div>
          ) : history?.content.length === 0 ? (
            <p className="text-center text-gray-400 py-8">No rewards yet. Complete missions to earn rewards!</p>
          ) : (
            <div className="space-y-3">
              {history?.content.map(reward => (
                <div key={reward.id} className="flex items-center justify-between py-3 border-b border-gray-100 last:border-0">
                  <div className="flex items-center gap-3">
                    <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-yellow-50">
                      <Gift className="h-4 w-4 text-yellow-500" />
                    </div>
                    <div>
                      <p className="text-sm font-medium text-gray-800">
                        {reward.catalog || (reward.diceCount ? `${reward.diceCount} dice roll(s)` : 'Mystery reward')}
                      </p>
                      <p className="text-xs text-gray-400">
                        {sourceLabels[reward.rewardSource] ?? reward.rewardSource}
                      </p>
                    </div>
                  </div>
                  <span className="text-xs text-gray-400">
                    {new Date(reward.earnedAt).toLocaleDateString()}
                  </span>
                </div>
              ))}
            </div>
          )}

          {history && history.totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 mt-4">
              <Button variant="ghost" size="sm" onClick={() => setPage(p => p - 1)} disabled={page === 0}>
                <ChevronLeft className="h-4 w-4" />
              </Button>
              <span className="text-sm text-gray-600">Page {page + 1} of {history.totalPages}</span>
              <Button variant="ghost" size="sm" onClick={() => setPage(p => p + 1)} disabled={page >= history.totalPages - 1}>
                <ChevronRight className="h-4 w-4" />
              </Button>
            </div>
          )}
        </CardBody>
      </Card>
    </div>
  )
}

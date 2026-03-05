import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { CheckCircle2, Gift, Dice1 } from 'lucide-react'
import { missionsApi } from '../api/missions'
import { Card, CardBody, CardHeader } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { Spinner } from '../components/ui/Spinner'

function ProgressBar({ value, max }: { value: number; max: number }) {
  const pct = max > 0 ? Math.min(100, Math.round((value / max) * 100)) : 0
  return (
    <div>
      <div className="flex justify-between text-xs text-gray-500 mb-1">
        <span>{value} / {max}</span>
        <span>{pct}%</span>
      </div>
      <div className="h-2 rounded-full bg-gray-200">
        <div className="h-2 rounded-full bg-blue-500 transition-all" style={{ width: `${pct}%` }} />
      </div>
    </div>
  )
}

export function MissionsPage() {
  const queryClient = useQueryClient()

  const { data: daily, isLoading: dailyLoading } = useQuery({
    queryKey: ['daily-mission'],
    queryFn: () => missionsApi.getDaily().then(r => r.data.data),
  })

  const { data: weekly, isLoading: weeklyLoading } = useQuery({
    queryKey: ['weekly-mission'],
    queryFn: () => missionsApi.getWeekly().then(r => r.data.data),
  })

  const claimSpin = useMutation({
    mutationFn: () => missionsApi.claimDailySpin(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['daily-mission'] })
      queryClient.invalidateQueries({ queryKey: ['rewards'] })
    },
  })

  const claimDice = useMutation({
    mutationFn: () => missionsApi.claimWeeklyDice(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['weekly-mission'] })
      queryClient.invalidateQueries({ queryKey: ['rewards'] })
    },
  })

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Missions</h1>

      {/* Daily mission */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <h2 className="font-semibold text-gray-900">Daily Mission</h2>
            {daily?.isCompleted && (
              <CheckCircle2 className="h-5 w-5 text-green-500" />
            )}
          </div>
        </CardHeader>
        <CardBody className="space-y-4">
          {dailyLoading ? (
            <Spinner />
          ) : daily ? (
            <>
              <div className="space-y-3">
                <div>
                  <p className="text-sm font-medium text-gray-700 mb-1.5">
                    Words reaching EASY
                  </p>
                  <ProgressBar value={daily.easyWordsAchieved} max={daily.easyWordsTarget} />
                </div>
                <div>
                  <p className="text-sm font-medium text-gray-700 mb-1.5">
                    Application reviews
                  </p>
                  <ProgressBar value={daily.appReviewAchieved} max={daily.appReviewTarget} />
                </div>
              </div>

              {daily.isCompleted && !daily.spinClaimed && (
                <Button
                  className="w-full"
                  onClick={() => claimSpin.mutate()}
                  loading={claimSpin.isPending}
                >
                  <Gift className="h-4 w-4" />
                  Claim daily spin reward!
                </Button>
              )}
              {daily.spinClaimed && (
                <p className="text-center text-sm text-green-600 font-medium">
                  Daily spin claimed!
                </p>
              )}
              {claimSpin.data && (
                <div className="rounded-lg bg-green-50 border border-green-200 p-3 text-center">
                  <p className="text-sm font-semibold text-green-700">
                    You won: {claimSpin.data.data.data.catalog || 'Surprise reward!'}
                  </p>
                </div>
              )}
            </>
          ) : null}
        </CardBody>
      </Card>

      {/* Weekly mission */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <h2 className="font-semibold text-gray-900">Weekly Mission</h2>
            {weekly?.isCompleted && (
              <CheckCircle2 className="h-5 w-5 text-green-500" />
            )}
          </div>
        </CardHeader>
        <CardBody className="space-y-4">
          {weeklyLoading ? (
            <Spinner />
          ) : weekly ? (
            <>
              <div className="space-y-3">
                <div>
                  <p className="text-sm font-medium text-gray-700 mb-1.5">Words reaching EASY</p>
                  <ProgressBar value={weekly.easyWordsAchieved} max={weekly.easyWordsTarget} />
                </div>
                <div>
                  <p className="text-sm font-medium text-gray-700 mb-1.5">Application reviews</p>
                  <ProgressBar value={weekly.appReviewAchieved} max={weekly.appReviewTarget} />
                </div>
              </div>

              {weekly.isCompleted && !weekly.diceClaimed && (
                <Button
                  className="w-full"
                  onClick={() => claimDice.mutate()}
                  loading={claimDice.isPending}
                >
                  <Dice1 className="h-4 w-4" />
                  Claim weekly dice reward!
                </Button>
              )}
              {weekly.diceClaimed && (
                <p className="text-center text-sm text-green-600 font-medium">
                  Weekly dice claimed!
                </p>
              )}
              {claimDice.data && (
                <div className="rounded-lg bg-green-50 border border-green-200 p-3 text-center">
                  <p className="text-sm font-semibold text-green-700">
                    You won {claimDice.data.data.data.diceCount} dice rolls!
                  </p>
                </div>
              )}
            </>
          ) : null}
        </CardBody>
      </Card>
    </div>
  )
}

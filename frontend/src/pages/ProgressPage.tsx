import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Flame, BookOpen, Library, RotateCcw, Trophy } from 'lucide-react'
import { progressApi } from '../api/progress'
import { Card, CardBody, CardHeader } from '../components/ui/Card'
import { Spinner } from '../components/ui/Spinner'

function CircleProgress({ value, max, label, icon: Icon, color }: {
  value: number
  max: number
  label: string
  icon: React.ElementType
  color: string
}) {
  const pct = max > 0 ? Math.min(1, value / max) : 0
  const radius = 36
  const circumference = 2 * Math.PI * radius
  const strokeDashoffset = circumference * (1 - pct)

  return (
    <div className="flex flex-col items-center gap-2">
      <div className="relative h-24 w-24">
        <svg className="h-24 w-24 -rotate-90" viewBox="0 0 88 88">
          <circle cx="44" cy="44" r={radius} fill="none" stroke="#e5e7eb" strokeWidth="8" />
          <circle
            cx="44" cy="44" r={radius}
            fill="none"
            stroke={color}
            strokeWidth="8"
            strokeLinecap="round"
            strokeDasharray={circumference}
            strokeDashoffset={strokeDashoffset}
            className="transition-all duration-500"
          />
        </svg>
        <div className="absolute inset-0 flex items-center justify-center">
          <Icon className="h-6 w-6" style={{ color }} />
        </div>
      </div>
      <div className="text-center">
        <p className="text-sm font-semibold text-gray-800">{label}</p>
        <p className="text-xs text-gray-400">{Math.round(pct * 100)}%</p>
      </div>
    </div>
  )
}

export function ProgressPage() {
  const [historyFrom] = useState(() => {
    const d = new Date(); d.setDate(d.getDate() - 30); return d.toISOString().split('T')[0]
  })
  const [historyTo] = useState(() => new Date().toISOString().split('T')[0])

  const { data: today, isLoading } = useQuery({
    queryKey: ['progress-today'],
    queryFn: () => progressApi.getToday().then(r => r.data.data),
  })

  const { data: streak } = useQuery({
    queryKey: ['streak'],
    queryFn: () => progressApi.getStreak().then(r => r.data.data),
  })

  const { data: history } = useQuery({
    queryKey: ['progress-history', historyFrom, historyTo],
    queryFn: () => progressApi.getHistory(historyFrom, historyTo).then(r => r.data.data),
  })

  if (isLoading) return <div className="flex justify-center py-20"><Spinner /></div>

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Progress</h1>

      {/* Streak banner */}
      {streak && (
        <div className="rounded-2xl bg-gradient-to-r from-orange-500 to-amber-400 p-6 text-white">
          <div className="flex items-center justify-between">
            <div>
              <div className="flex items-center gap-2 mb-1">
                <Flame className="h-6 w-6" />
                <span className="text-2xl font-bold">{streak.currentStreak} day streak</span>
              </div>
              <p className="text-orange-100 text-sm">Longest: {streak.longestStreak} days</p>
            </div>
            <Trophy className="h-12 w-12 text-orange-200" />
          </div>
        </div>
      )}

      {/* Today's progress */}
      {today && (
        <Card>
          <CardHeader>
            <h2 className="font-semibold text-gray-900">Today — {today.date}</h2>
          </CardHeader>
          <CardBody>
            <div className="flex justify-around">
              <CircleProgress
                value={today.reading.seconds}
                max={today.reading.goalSeconds}
                label="Reading"
                icon={BookOpen}
                color="#3b82f6"
              />
              <CircleProgress
                value={today.vocab.wordsReachedEasy}
                max={today.vocab.goal}
                label="Vocabulary"
                icon={Library}
                color="#8b5cf6"
              />
              <CircleProgress
                value={today.review.completed}
                max={today.review.goal}
                label="Reviews"
                icon={RotateCcw}
                color="#10b981"
              />
            </div>

            <div className="mt-6 grid grid-cols-3 gap-4 text-center">
              <div>
                <p className="text-xs text-gray-400">Reading</p>
                <p className="font-semibold text-gray-800">
                  {Math.floor(today.reading.seconds / 60)}m / {Math.floor(today.reading.goalSeconds / 60)}m
                </p>
                {today.reading.achieved && (
                  <p className="text-xs text-green-600 font-medium">Achieved!</p>
                )}
              </div>
              <div>
                <p className="text-xs text-gray-400">Words (EASY)</p>
                <p className="font-semibold text-gray-800">
                  {today.vocab.wordsReachedEasy} / {today.vocab.goal}
                </p>
                {today.vocab.achieved && (
                  <p className="text-xs text-green-600 font-medium">Achieved!</p>
                )}
              </div>
              <div>
                <p className="text-xs text-gray-400">Reviews</p>
                <p className="font-semibold text-gray-800">
                  {today.review.completed} / {today.review.goal}
                </p>
                {today.review.achieved && (
                  <p className="text-xs text-green-600 font-medium">Achieved!</p>
                )}
              </div>
            </div>
          </CardBody>
        </Card>
      )}

      {/* History */}
      {history && history.length > 0 && (
        <Card>
          <CardHeader>
            <h2 className="font-semibold text-gray-900">Last 30 days</h2>
          </CardHeader>
          <CardBody>
            <div className="grid grid-cols-7 gap-1">
              {history.slice(-30).map(day => {
                const maintained = day.streakMaintained
                return (
                  <div
                    key={day.date}
                    title={`${day.date}: ${maintained ? 'Streak maintained' : 'Missed'}`}
                    className={`h-8 rounded ${maintained ? 'bg-green-400' : 'bg-gray-100'}`}
                  />
                )
              })}
            </div>
            <div className="mt-2 flex items-center gap-4 text-xs text-gray-400">
              <span className="flex items-center gap-1">
                <span className="h-3 w-3 rounded bg-green-400 inline-block" /> Streak maintained
              </span>
              <span className="flex items-center gap-1">
                <span className="h-3 w-3 rounded bg-gray-100 inline-block" /> Missed
              </span>
            </div>
          </CardBody>
        </Card>
      )}
    </div>
  )
}

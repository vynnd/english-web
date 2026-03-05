import { useState, useRef } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { CheckCircle, XCircle } from 'lucide-react'
import { applicationApi } from '../api/application'
import { Spinner } from '../components/ui/Spinner'
import { Button } from '../components/ui/Button'
import { Card, CardBody } from '../components/ui/Card'
import { Badge } from '../components/ui/Badge'

const levelLabels = ['', 'NEW', 'RECOGNITION', 'RECALL', 'PRODUCTION', 'USAGE', 'INTEGRATED USAGE']

export function ApplicationPage() {
  const { uvId } = useParams<{ uvId: string }>()
  const queryClient = useQueryClient()
  const [answer, setAnswer] = useState('')
  const [result, setResult] = useState<{ correct: boolean; feedback: string; newLevel: number; pointsEarned: number } | null>(null)
  const startTimeRef = useRef<number>(Date.now())

  const { data: status, isLoading: statusLoading } = useQuery({
    queryKey: ['app-status', uvId],
    queryFn: () => applicationApi.getStatus(uvId!).then(r => r.data.data),
    enabled: !!uvId,
  })

  const { data: task, isLoading: taskLoading, refetch: refetchTask } = useQuery({
    queryKey: ['app-task', uvId],
    queryFn: () => applicationApi.getNextTask(uvId!).then(r => r.data.data),
    enabled: !!uvId && !!status?.nextTaskAvailable,
  })

  const submitMutation = useMutation({
    mutationFn: () =>
      applicationApi.submitTask(uvId!, task!.taskId, answer, Date.now() - startTimeRef.current),
    onSuccess: (res) => {
      setResult(res.data.data)
      queryClient.invalidateQueries({ queryKey: ['app-status', uvId] })
    },
  })

  const handleNext = () => {
    setResult(null)
    setAnswer('')
    startTimeRef.current = Date.now()
    refetchTask()
    queryClient.invalidateQueries({ queryKey: ['app-task', uvId] })
  }

  if (statusLoading) return <div className="flex justify-center py-20"><Spinner /></div>
  if (!status) return <p className="text-center text-gray-500">Not found.</p>

  return (
    <div className="mx-auto max-w-lg space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Application Assessment</h1>
        <div className="mt-2 flex items-center gap-3">
          <Badge color="blue">LV{status.currentLevel} — {levelLabels[status.currentLevel]}</Badge>
          <span className="text-sm text-gray-500">{status.tasksCompleted} tasks done</span>
        </div>
      </div>

      {!status.nextTaskAvailable ? (
        <div className="rounded-2xl border border-gray-200 bg-white p-10 text-center">
          <CheckCircle className="mx-auto h-12 w-12 text-green-500 mb-4" />
          <h2 className="text-xl font-bold text-gray-900">Level Mastered!</h2>
          <p className="mt-2 text-gray-500">
            You've reached <strong>LV{status.currentLevel}</strong> — {levelLabels[status.currentLevel]}.
            Keep using this word in context to reach the next level.
          </p>
        </div>
      ) : taskLoading ? (
        <div className="flex justify-center py-16"><Spinner /></div>
      ) : task ? (
        <div className="space-y-4">
          <Card>
            <CardBody>
              <p className="text-xs text-gray-400 uppercase tracking-wide mb-2">{task.taskType}</p>
              <p className="text-lg text-gray-900">{task.prompt}</p>
            </CardBody>
          </Card>

          {result ? (
            <div className="space-y-4">
              <div className={`rounded-xl p-4 ${result.correct ? 'bg-green-50 border border-green-200' : 'bg-red-50 border border-red-200'}`}>
                <div className="flex items-center gap-2 mb-2">
                  {result.correct
                    ? <CheckCircle className="h-5 w-5 text-green-600" />
                    : <XCircle className="h-5 w-5 text-red-500" />
                  }
                  <span className={`font-semibold ${result.correct ? 'text-green-700' : 'text-red-600'}`}>
                    {result.correct ? 'Correct!' : 'Not quite'}
                  </span>
                  {result.pointsEarned > 0 && (
                    <span className="ml-auto text-sm text-green-600 font-medium">+{result.pointsEarned} pts</span>
                  )}
                </div>
                <p className="text-sm text-gray-700">{result.feedback}</p>
                {result.newLevel > status.currentLevel && (
                  <p className="mt-2 text-sm font-semibold text-blue-700">
                    Level up! Now LV{result.newLevel} — {levelLabels[result.newLevel]}
                  </p>
                )}
              </div>
              <Button className="w-full" onClick={handleNext}>Next task</Button>
            </div>
          ) : (
            <div className="space-y-4">
              {task.options ? (
                <div className="grid gap-2">
                  {task.options.map((opt, i) => (
                    <button
                      key={i}
                      onClick={() => setAnswer(opt)}
                      className={`rounded-xl border-2 px-4 py-3 text-left text-sm font-medium transition-colors ${
                        answer === opt
                          ? 'border-blue-500 bg-blue-50 text-blue-700'
                          : 'border-gray-200 bg-white text-gray-700 hover:border-gray-300'
                      }`}
                    >
                      {opt}
                    </button>
                  ))}
                </div>
              ) : (
                <textarea
                  value={answer}
                  onChange={(e) => setAnswer(e.target.value)}
                  placeholder="Type your answer here..."
                  className="w-full rounded-xl border border-gray-300 p-3 text-sm outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                  rows={4}
                />
              )}
              <Button
                className="w-full"
                disabled={!answer.trim()}
                loading={submitMutation.isPending}
                onClick={() => submitMutation.mutate()}
              >
                Submit
              </Button>
            </div>
          )}
        </div>
      ) : null}
    </div>
  )
}

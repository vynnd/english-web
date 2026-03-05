import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Lock, CheckCircle2, Circle } from 'lucide-react'
import { weeklyReviewApi } from '../api/weeklyReview'
import { Spinner } from '../components/ui/Spinner'
import { Badge } from '../components/ui/Badge'
import { Button } from '../components/ui/Button'
import { Card, CardBody, CardHeader } from '../components/ui/Card'

const appLevelLabels = ['', 'NEW', 'RECOGNITION', 'RECALL', 'PRODUCTION', 'USAGE', 'INTEGRATED']

export function WeeklyReviewPage() {
  const queryClient = useQueryClient()

  const { data: review, isLoading } = useQuery({
    queryKey: ['weekly-review'],
    queryFn: () => weeklyReviewApi.getCurrent().then(r => r.data.data),
  })

  const { data: tasks } = useQuery({
    queryKey: ['weekly-tasks'],
    queryFn: () => weeklyReviewApi.getTasks().then(r => r.data.data),
    enabled: !!review?.isUnlocked,
  })

  const completeTask = useMutation({
    mutationFn: (taskId: string) => weeklyReviewApi.completeTask(taskId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['weekly-tasks'] }),
  })

  if (isLoading) return <div className="flex justify-center py-20"><Spinner /></div>

  if (!review?.isUnlocked) {
    return (
      <div className="flex flex-col items-center justify-center py-20 text-center space-y-4">
        <Lock className="h-16 w-16 text-gray-300" />
        <h2 className="text-2xl font-bold text-gray-900">Weekly Review Locked</h2>
        <p className="text-gray-500 max-w-sm">
          You need at least 3 words at LV2 (RECOGNITION) or higher to unlock the weekly review set.
        </p>
      </div>
    )
  }

  const weekStart = review.weekStart ? new Date(review.weekStart).toLocaleDateString() : ''

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Weekly Review</h1>
        <p className="mt-1 text-sm text-gray-500">Week of {weekStart}</p>
      </div>

      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
        {review.words.map(w => (
          <Card key={w.userVocabularyId}>
            <CardBody className="space-y-2">
              <p className="font-semibold text-gray-900 text-lg">{w.word}</p>
              <Badge color="blue">LV{w.appLevel} — {appLevelLabels[w.appLevel]}</Badge>
            </CardBody>
          </Card>
        ))}
      </div>

      {tasks && tasks.length > 0 && (
        <Card>
          <CardHeader>
            <h2 className="font-semibold text-gray-900">Weekly Tasks</h2>
          </CardHeader>
          <CardBody className="space-y-3">
            {tasks.map(task => (
              <div key={task.id} className="flex items-start gap-3 py-2">
                <div className="mt-0.5">
                  {task.isCompleted
                    ? <CheckCircle2 className="h-5 w-5 text-green-500" />
                    : <Circle className="h-5 w-5 text-gray-300" />
                  }
                </div>
                <div className="flex-1">
                  <p className="text-sm font-medium text-gray-800">{task.word}</p>
                  <p className="text-sm text-gray-500">{task.description}</p>
                  <Badge color="gray" className="mt-1">{task.taskType}</Badge>
                </div>
                {!task.isCompleted && (
                  <Button
                    size="sm"
                    variant="secondary"
                    loading={completeTask.isPending}
                    onClick={() => completeTask.mutate(task.id)}
                  >
                    Mark done
                  </Button>
                )}
              </div>
            ))}
          </CardBody>
        </Card>
      )}
    </div>
  )
}

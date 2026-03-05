import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { Trash2, Zap, ChevronLeft, ChevronRight } from 'lucide-react'
import { vocabularyApi } from '../api/vocabulary'
import { Card, CardBody } from '../components/ui/Card'
import { Badge } from '../components/ui/Badge'
import { Button } from '../components/ui/Button'
import { Spinner } from '../components/ui/Spinner'

const appLevelLabels = ['', 'NEW', 'RECOGNITION', 'RECALL', 'PRODUCTION', 'USAGE', 'INTEGRATED USAGE']
const appLevelColors: ('gray' | 'blue' | 'yellow' | 'green' | 'purple' | 'red')[] = ['gray', 'gray', 'blue', 'yellow', 'green', 'purple', 'red']

export function VocabularyPage() {
  const [page, setPage] = useState(0)
  const queryClient = useQueryClient()

  const { data: limitData } = useQuery({
    queryKey: ['vocab-limit'],
    queryFn: () => vocabularyApi.getDailyLimit().then(r => r.data.data),
  })

  const { data, isLoading } = useQuery({
    queryKey: ['vocabulary', page],
    queryFn: () => vocabularyApi.list({ page, size: 20 }).then(r => r.data.data),
  })

  const deleteMutation = useMutation({
    mutationFn: (id: string) => vocabularyApi.delete(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['vocabulary'] }),
  })

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Vocabulary</h1>
          <p className="mt-1 text-sm text-gray-500">Your saved words and their learning progress</p>
        </div>
        {limitData && (
          <div className="text-right">
            <p className="text-sm text-gray-500">Daily saves</p>
            <p className="font-semibold text-gray-900">
              {limitData.saved} / {limitData.limit}
            </p>
          </div>
        )}
      </div>

      {isLoading ? (
        <div className="flex justify-center py-16"><Spinner /></div>
      ) : (
        <>
          <div className="space-y-3">
            {data?.content.length === 0 && (
              <div className="rounded-2xl border border-dashed border-gray-300 py-16 text-center">
                <p className="text-gray-400">No words saved yet.</p>
                <p className="mt-1 text-sm text-gray-400">
                  Start reading articles and click on words to look them up.
                </p>
              </div>
            )}
            {data?.content.map(entry => (
              <Card key={entry.id}>
                <CardBody className="flex items-center justify-between gap-4">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="font-semibold text-gray-900">{entry.word}</span>
                      {entry.phonetic && (
                        <span className="text-xs text-gray-400">{entry.phonetic}</span>
                      )}
                      {entry.partOfSpeech && (
                        <span className="text-xs italic text-gray-400">{entry.partOfSpeech}</span>
                      )}
                    </div>
                    <p className="mt-0.5 text-sm text-gray-600 truncate">
                      {entry.vnMeaning || entry.definitions?.[0]}
                    </p>
                    <div className="mt-2 flex items-center gap-2">
                      <Badge color={appLevelColors[entry.appLevel] ?? 'gray'}>
                        LV{entry.appLevel} {appLevelLabels[entry.appLevel]}
                      </Badge>
                      {entry.nextReviewAt && (
                        <span className="text-xs text-gray-400">
                          Next review: {new Date(entry.nextReviewAt).toLocaleDateString()}
                        </span>
                      )}
                    </div>
                  </div>
                  <div className="flex items-center gap-2 shrink-0">
                    <Link to={`/application/${entry.id}`}>
                      <Button variant="ghost" size="sm">
                        <Zap className="h-4 w-4 text-yellow-500" />
                      </Button>
                    </Link>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => {
                        if (confirm(`Remove "${entry.word}" from vocabulary?`)) {
                          deleteMutation.mutate(entry.id)
                        }
                      }}
                    >
                      <Trash2 className="h-4 w-4 text-red-400" />
                    </Button>
                  </div>
                </CardBody>
              </Card>
            ))}
          </div>

          {data && data.totalPages > 1 && (
            <div className="flex items-center justify-center gap-2">
              <Button variant="ghost" size="sm" onClick={() => setPage(p => p - 1)} disabled={page === 0}>
                <ChevronLeft className="h-4 w-4" />
              </Button>
              <span className="text-sm text-gray-600">Page {page + 1} of {data.totalPages}</span>
              <Button variant="ghost" size="sm" onClick={() => setPage(p => p + 1)} disabled={page >= data.totalPages - 1}>
                <ChevronRight className="h-4 w-4" />
              </Button>
            </div>
          )}
        </>
      )}
    </div>
  )
}

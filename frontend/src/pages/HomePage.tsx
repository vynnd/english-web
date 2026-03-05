import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Clock, BookOpen, ChevronLeft, ChevronRight } from 'lucide-react'
import { articlesApi } from '../api/articles'
import { Card, CardBody } from '../components/ui/Card'
import { Badge } from '../components/ui/Badge'
import { Spinner } from '../components/ui/Spinner'
import { Button } from '../components/ui/Button'

const levelColors: Record<string, 'blue' | 'green' | 'yellow' | 'red' | 'purple' | 'gray'> = {
  A1: 'green', A2: 'green', B1: 'blue', B2: 'blue', C1: 'purple', C2: 'red'
}

export function HomePage() {
  const [topicId, setTopicId] = useState<string | undefined>()
  const [level, setLevel] = useState<string | undefined>()
  const [page, setPage] = useState(0)

  const { data: topicsData } = useQuery({
    queryKey: ['topics'],
    queryFn: () => articlesApi.getTopics().then(r => r.data.data),
  })

  const { data, isLoading } = useQuery({
    queryKey: ['articles', topicId, level, page],
    queryFn: () => articlesApi.list({ topicId, level, page, size: 12 }).then(r => r.data.data),
  })

  const handleFilterChange = (newTopicId?: string, newLevel?: string) => {
    setTopicId(newTopicId)
    setLevel(newLevel)
    setPage(0)
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Reading</h1>
        <p className="mt-1 text-sm text-gray-500">Read articles and build your vocabulary</p>
      </div>

      {/* Filters */}
      <div className="flex flex-wrap gap-2">
        <button
          onClick={() => handleFilterChange(undefined, undefined)}
          className={`rounded-full px-4 py-1.5 text-sm font-medium transition-colors ${!topicId && !level ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}
        >
          All
        </button>
        {topicsData?.map(topic => (
          <button
            key={topic.id}
            onClick={() => handleFilterChange(topic.id, level)}
            className={`rounded-full px-4 py-1.5 text-sm font-medium transition-colors ${topicId === topic.id ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}
          >
            {topic.name}
          </button>
        ))}
        {['A1', 'A2', 'B1', 'B2', 'C1', 'C2'].map(lvl => (
          <button
            key={lvl}
            onClick={() => handleFilterChange(topicId, level === lvl ? undefined : lvl)}
            className={`rounded-full px-3 py-1.5 text-sm font-medium transition-colors ${level === lvl ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}
          >
            {lvl}
          </button>
        ))}
      </div>

      {isLoading ? (
        <div className="flex justify-center py-16">
          <Spinner />
        </div>
      ) : (
        <>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {data?.content.map(article => (
              <Link key={article.id} to={`/articles/${article.id}`}>
                <Card className="h-full transition-shadow hover:shadow-md">
                  <CardBody className="space-y-3">
                    <div className="flex items-start justify-between gap-2">
                      <h3 className="font-semibold text-gray-900 leading-snug line-clamp-2">
                        {article.title}
                      </h3>
                      {article.languageLevel && (
                        <Badge color={levelColors[article.languageLevel] ?? 'gray'} className="shrink-0">
                          {article.languageLevel}
                        </Badge>
                      )}
                    </div>
                    <div className="flex items-center gap-4 text-xs text-gray-400">
                      <span className="flex items-center gap-1">
                        <BookOpen className="h-3 w-3" />
                        {article.wordCount} words
                      </span>
                      {article.estimatedReadSeconds > 0 && (
                        <span className="flex items-center gap-1">
                          <Clock className="h-3 w-3" />
                          {Math.ceil(article.estimatedReadSeconds / 60)} min
                        </span>
                      )}
                    </div>
                  </CardBody>
                </Card>
              </Link>
            ))}
          </div>

          {/* Pagination */}
          {data && data.totalPages > 1 && (
            <div className="flex items-center justify-center gap-2">
              <Button variant="ghost" size="sm" onClick={() => setPage(p => p - 1)} disabled={page === 0}>
                <ChevronLeft className="h-4 w-4" />
              </Button>
              <span className="text-sm text-gray-600">
                Page {page + 1} of {data.totalPages}
              </span>
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

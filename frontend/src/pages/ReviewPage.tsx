import { useState, useRef } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { CheckCircle, XCircle, RotateCcw } from 'lucide-react'
import { vocabularyApi } from '../api/vocabulary'
import { srsApi } from '../api/srs'
import { Button } from '../components/ui/Button'
import { Spinner } from '../components/ui/Spinner'
import { Card, CardBody } from '../components/ui/Card'

type CardSide = 'word' | 'meaning'
type ReviewPhase = 'question' | 'answer' | 'done'

export function ReviewPage() {
  const queryClient = useQueryClient()
  const [currentIdx, setCurrentIdx] = useState(0)
  const [showSide, setShowSide] = useState<CardSide>('word')
  const [phase, setPhase] = useState<ReviewPhase>('question')
  const [results, setResults] = useState<{ word: string; correct: boolean }[]>([])
  const startTimeRef = useRef<number>(Date.now())
  const [flipped, setFlipped] = useState(false)

  const { data: dueWords, isLoading, refetch } = useQuery({
    queryKey: ['due-words'],
    queryFn: () => vocabularyApi.getDue().then(r => r.data.data),
  })

  const reviewMutation = useMutation({
    mutationFn: ({ uvId, responseTimeMs }: { uvId: string; responseTimeMs: number }) =>
      srsApi.submitReview(uvId, responseTimeMs),
  })

  const handleFlip = () => setFlipped(true)

  const handleAnswer = async (correct: boolean) => {
    if (!dueWords) return
    const word = dueWords[currentIdx]
    const responseTimeMs = Date.now() - startTimeRef.current

    await reviewMutation.mutateAsync({ uvId: word.userVocabularyId, responseTimeMs })
    setResults(prev => [...prev, { word: word.word, correct }])

    const next = currentIdx + 1
    if (next >= dueWords.length) {
      setPhase('done')
    } else {
      setCurrentIdx(next)
      setFlipped(false)
      setShowSide(next % 2 === 0 ? 'word' : 'meaning')
      startTimeRef.current = Date.now()
    }
  }

  const handleRestart = () => {
    setCurrentIdx(0)
    setResults([])
    setPhase('question')
    setFlipped(false)
    startTimeRef.current = Date.now()
    refetch()
    queryClient.invalidateQueries({ queryKey: ['due-words'] })
  }

  if (isLoading) {
    return <div className="flex justify-center py-20"><Spinner /></div>
  }

  if (!dueWords || dueWords.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-20 text-center">
        <CheckCircle className="h-16 w-16 text-green-500 mb-4" />
        <h2 className="text-2xl font-bold text-gray-900">All caught up!</h2>
        <p className="mt-2 text-gray-500">No words due for review right now. Come back later.</p>
      </div>
    )
  }

  if (phase === 'done') {
    const correct = results.filter(r => r.correct).length
    return (
      <div className="flex flex-col items-center justify-center py-16 text-center space-y-6">
        <div>
          <h2 className="text-3xl font-bold text-gray-900">Session complete!</h2>
          <p className="mt-2 text-gray-500">
            {correct} / {results.length} correct
          </p>
        </div>
        <div className="w-full max-w-xs space-y-2">
          {results.map((r, i) => (
            <div key={i} className="flex items-center justify-between rounded-lg px-4 py-2 bg-gray-50">
              <span className="text-sm font-medium text-gray-700">{r.word}</span>
              {r.correct
                ? <CheckCircle className="h-4 w-4 text-green-500" />
                : <XCircle className="h-4 w-4 text-red-400" />
              }
            </div>
          ))}
        </div>
        <Button onClick={handleRestart}>
          <RotateCcw className="h-4 w-4" />
          Review again
        </Button>
      </div>
    )
  }

  const current = dueWords[currentIdx]
  const progress = ((currentIdx) / dueWords.length) * 100

  return (
    <div className="mx-auto max-w-lg space-y-6">
      <div>
        <div className="flex items-center justify-between mb-2">
          <h1 className="text-xl font-bold text-gray-900">SRS Review</h1>
          <span className="text-sm text-gray-500">{currentIdx + 1} / {dueWords.length}</span>
        </div>
        <div className="h-2 rounded-full bg-gray-200">
          <div
            className="h-2 rounded-full bg-blue-500 transition-all"
            style={{ width: `${progress}%` }}
          />
        </div>
      </div>

      {/* Flashcard */}
      <div className="min-h-64 rounded-2xl border-2 border-gray-200 bg-white p-8 flex flex-col items-center justify-center text-center shadow-sm">
        {showSide === 'word' ? (
          <>
            <p className="text-xs text-gray-400 mb-4 uppercase tracking-wider">Word</p>
            <p className="text-4xl font-bold text-gray-900">{current.word}</p>
            {current.phonetic && (
              <p className="mt-2 text-sm text-gray-400">{current.phonetic}</p>
            )}
            {current.partOfSpeech && (
              <p className="mt-1 text-sm italic text-gray-400">{current.partOfSpeech}</p>
            )}
          </>
        ) : (
          <>
            <p className="text-xs text-gray-400 mb-4 uppercase tracking-wider">Meaning</p>
            <p className="text-lg text-gray-700">{current.definitions?.[0]}</p>
          </>
        )}
      </div>

      {!flipped ? (
        <Button className="w-full" size="lg" onClick={handleFlip}>
          Show {showSide === 'word' ? 'meaning' : 'word'}
        </Button>
      ) : (
        <div className="space-y-3">
          {/* Show the other side */}
          <Card>
            <CardBody className="text-center">
              {showSide === 'word' ? (
                <>
                  <p className="text-lg text-gray-800">{current.definitions?.[0]}</p>
                  {current.examples?.[0] && (
                    <p className="mt-2 text-sm italic text-gray-400">"{current.examples[0]}"</p>
                  )}
                </>
              ) : (
                <p className="text-2xl font-bold text-gray-900">{current.word}</p>
              )}
            </CardBody>
          </Card>

          <div className="grid grid-cols-2 gap-3">
            <Button
              variant="danger"
              size="lg"
              onClick={() => handleAnswer(false)}
              loading={reviewMutation.isPending}
            >
              <XCircle className="h-5 w-5" />
              Didn't know
            </Button>
            <Button
              size="lg"
              className="bg-green-600 hover:bg-green-700"
              onClick={() => handleAnswer(true)}
              loading={reviewMutation.isPending}
            >
              <CheckCircle className="h-5 w-5" />
              Got it!
            </Button>
          </div>
        </div>
      )}
    </div>
  )
}

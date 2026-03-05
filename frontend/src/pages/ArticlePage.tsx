import { useState, useRef, useEffect, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation } from '@tanstack/react-query'
import { ArrowLeft, BookmarkPlus, X, Volume2 } from 'lucide-react'
import { articlesApi } from '../api/articles'
import { wordsApi, type WordDetail, type WordTooltip } from '../api/words'
import { readingSessionsApi, type PendingWord } from '../api/readingSessions'
import { vocabularyApi } from '../api/vocabulary'
import { Spinner } from '../components/ui/Spinner'
import { Button } from '../components/ui/Button'
import { Modal } from '../components/ui/Modal'
import { Badge } from '../components/ui/Badge'

interface TooltipState {
  word: string
  x: number
  y: number
  data: WordTooltip | null
}

interface SavePopupState {
  open: boolean
  words: PendingWord[]
  sessionId: string
  articleId: string
  selectedIds: Set<string>
}

export function ArticlePage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const startTimeRef = useRef<number>(Date.now())
  const sessionIdRef = useRef<string | null>(null)
  const longPressTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null)
  const isLongPressRef = useRef(false)

  const [tooltip, setTooltip] = useState<TooltipState | null>(null)
  const [wordModal, setWordModal] = useState<WordDetail | null>(null)
  const [wordModalLoading, setWordModalLoading] = useState(false)
  const [savePopup, setSavePopup] = useState<SavePopupState>({
    open: false, words: [], sessionId: '', articleId: '', selectedIds: new Set(),
  })

  const { data: article, isLoading } = useQuery({
    queryKey: ['article', id],
    queryFn: () => articlesApi.get(id!).then(r => r.data.data),
    enabled: !!id,
  })

  // Start reading session when article loads
  useEffect(() => {
    if (!id) return
    readingSessionsApi.start(id).then(res => {
      sessionIdRef.current = res.data.data.sessionId
      startTimeRef.current = Date.now()
    }).catch(() => {})

    return () => {
      // End session on unmount
      if (sessionIdRef.current) {
        const durationSeconds = Math.floor((Date.now() - startTimeRef.current) / 1000)
        readingSessionsApi.end(sessionIdRef.current, durationSeconds).then(async () => {
          const sessionId = sessionIdRef.current!
          const pending = await readingSessionsApi.getPendingWords(sessionId)
          if (pending.data.data.length > 0) {
            setSavePopup({
              open: true,
              words: pending.data.data,
              sessionId,
              articleId: id,
              selectedIds: new Set(pending.data.data.map(w => w.wordId)),
            })
          }
        }).catch(() => {})
      }
    }
  }, [id])

  // Hide tooltip on outside click
  useEffect(() => {
    const handler = () => setTooltip(null)
    document.addEventListener('click', handler)
    return () => document.removeEventListener('click', handler)
  }, [])

  const handleWordClick = useCallback(async (word: string, e: React.MouseEvent) => {
    e.stopPropagation()
    if (isLongPressRef.current) return  // long press already handled — skip click

    try {
      const res = await wordsApi.getTooltip(word)
      const wordData = res.data.data

      // Track click using the real word UUID
      if (sessionIdRef.current && wordData.id) {
        readingSessionsApi.trackWordClick(sessionIdRef.current, wordData.id).catch(() => {})
      }

      const rect = (e.target as HTMLElement).getBoundingClientRect()
      const x = Math.min(rect.left, window.innerWidth - 300)
      const y = rect.bottom + 8
      setTooltip({ word, x, y, data: wordData })
    } catch {}
  }, [])

  const handleWordLongPress = useCallback(async (word: string) => {
    setWordModalLoading(true)
    setWordModal(null)
    try {
      const res = await wordsApi.getDetail(word)
      setWordModal(res.data.data)
    } catch {} finally {
      setWordModalLoading(false)
    }
  }, [])

  const saveWords = useMutation({
    mutationFn: () =>
      vocabularyApi.save(Array.from(savePopup.selectedIds), savePopup.articleId),
    onSuccess: () => setSavePopup(prev => ({ ...prev, open: false })),
  })

  const playWord = useCallback((word: string, audioUrl?: string) => {
    if (audioUrl) {
      new Audio(audioUrl).play()
    } else {
      const utter = new SpeechSynthesisUtterance(word)
      utter.lang = 'en-US'
      window.speechSynthesis.cancel()
      window.speechSynthesis.speak(utter)
    }
  }, [])

  if (isLoading) {
    return <div className="flex justify-center py-20"><Spinner /></div>
  }

  if (!article) {
    return <p className="text-center text-gray-500">Article not found.</p>
  }

  // Tokenize: extract words (including contractions like "don't") and non-word segments
  const tokens: Array<{ text: string; isWord: boolean }> = []
  const wordRegex = /[a-zA-Z]+(?:'[a-zA-Z]+)*/g
  let lastIndex = 0
  let m: RegExpExecArray | null
  while ((m = wordRegex.exec(article.content)) !== null) {
    if (m.index > lastIndex) tokens.push({ text: article.content.slice(lastIndex, m.index), isWord: false })
    tokens.push({ text: m[0], isWord: m[0].length >= 2 })
    lastIndex = m.index + m[0].length
  }
  if (lastIndex < article.content.length) tokens.push({ text: article.content.slice(lastIndex), isWord: false })

  return (
    <div className="space-y-6" onClick={() => setTooltip(null)}>
      <div className="flex items-center gap-3">
        <button onClick={() => navigate(-1)} className="rounded-lg p-2 text-gray-400 hover:bg-gray-100">
          <ArrowLeft className="h-5 w-5" />
        </button>
        <div>
          <h1 className="text-xl font-bold text-gray-900">{article.title}</h1>
          <div className="mt-1 flex items-center gap-3 text-xs text-gray-400">
            {article.languageLevel && <Badge color="blue">{article.languageLevel}</Badge>}
            <span>{article.wordCount} words</span>
            <span>{Math.ceil(article.estimatedReadSeconds / 60)} min read</span>
          </div>
        </div>
      </div>

      <div className="rounded-2xl border border-gray-200 bg-white p-8 leading-relaxed text-gray-800">
        <p className="text-base">
          {tokens.map(({ text, isWord }, idx) => {
            if (!isWord) return <span key={idx}>{text}</span>
            return (
              <span
                key={idx}
                className="cursor-pointer rounded hover:bg-yellow-100 hover:underline decoration-dotted"
                onClick={(e) => handleWordClick(text, e)}
                onMouseDown={() => {
                  isLongPressRef.current = false
                  longPressTimerRef.current = setTimeout(() => {
                    isLongPressRef.current = true
                    handleWordLongPress(text)
                  }, 1000)
                }}
                onMouseUp={() => { if (longPressTimerRef.current) clearTimeout(longPressTimerRef.current) }}
                onMouseLeave={() => { if (longPressTimerRef.current) clearTimeout(longPressTimerRef.current) }}
              >
                {text}
              </span>
            )
          })}
        </p>
      </div>

      {/* ── Tier 1: Tooltip (single click) ── */}
      {tooltip?.data && (
        <div
          className="fixed z-50 max-w-xs rounded-xl border border-gray-100 bg-white px-4 py-3 shadow-xl"
          style={{ left: tooltip.x, top: tooltip.y }}
          onClick={(e) => e.stopPropagation()}
        >
          <div className="flex items-start justify-between gap-3">
            <div className="flex-1 min-w-0">
              {/* Line 1: word (bold) + phonetic (blue) + audio button + pos (italic) */}
              <p className="flex flex-wrap items-center gap-1.5">
                <span className="text-base font-bold text-gray-900">{tooltip.data.word}</span>
                {tooltip.data.phonetic && (
                  <span className="text-sm text-blue-500">{tooltip.data.phonetic}</span>
                )}
                <button
                  onClick={() => playWord(tooltip.data!.word, tooltip.data!.audioUrl)}
                  className="text-gray-400 hover:text-blue-500 transition-colors"
                  title="Nghe phát âm"
                >
                  <Volume2 className="h-3.5 w-3.5" />
                </button>
                {tooltip.data.partOfSpeech && (
                  <span className="text-xs italic text-gray-400">{tooltip.data.partOfSpeech}</span>
                )}
              </p>
              {/* Line 2: Vietnamese meaning */}
              {tooltip.data.vnMeaning && (
                <p className="mt-1 text-sm text-gray-800">{tooltip.data.vnMeaning}</p>
              )}
              <button
                className="mt-2 text-xs font-medium text-blue-500 hover:text-blue-700"
                onClick={() => { setTooltip(null); handleWordLongPress(tooltip.word) }}
              >
                Xem chi tiết →
              </button>
            </div>
            <button onClick={() => setTooltip(null)} className="text-gray-300 hover:text-gray-500 shrink-0">
              <X className="h-3.5 w-3.5" />
            </button>
          </div>
        </div>
      )}

      {/* ── Tier 2: Full card modal (long press, Cambridge style) ── */}
      <Modal open={!!wordModal || wordModalLoading} onClose={() => setWordModal(null)} maxWidth="max-w-lg">
        {wordModalLoading ? (
          <div className="flex justify-center py-10"><Spinner /></div>
        ) : wordModal ? (
          <div>
            {/* Word header — stays visible while scrolling */}
            <div className="sticky top-0 z-10 mb-4 border-b border-gray-100 bg-white pt-5 pb-4">
              <div className="flex items-baseline gap-3">
                <h2 className="text-3xl font-bold text-gray-900">{wordModal.word}</h2>
                <button
                  onClick={() => playWord(wordModal.word, wordModal.audioUrl)}
                  className="text-gray-300 hover:text-blue-500 transition-colors"
                  title="Nghe phát âm"
                >
                  <Volume2 className="h-5 w-5" />
                </button>
              </div>
              <div className="mt-1 flex items-center gap-3">
                {wordModal.phonetic && (
                  <span className="text-base text-blue-500">{wordModal.phonetic}</span>
                )}
                {wordModal.partOfSpeech && (
                  <span className="rounded bg-blue-50 px-2 py-0.5 text-xs font-medium italic text-blue-600">
                    {wordModal.partOfSpeech}
                  </span>
                )}
              </div>
            </div>

            {/* Vietnamese meaning */}
            {wordModal.vnMeaning && (
              <div className="mb-4">
                <p className="mb-1 text-xs font-bold uppercase tracking-widest text-blue-600">Nghĩa tiếng Việt</p>
                <p className="text-sm text-gray-800">{wordModal.vnMeaning}</p>
              </div>
            )}

            {/* English definitions */}
            {wordModal.definitions?.filter(d => d.lang === 'en').length > 0 && (
              <div className="mb-4">
                <p className="mb-2 text-xs font-bold uppercase tracking-widest text-gray-400">Definitions</p>
                <ol className="space-y-2">
                  {wordModal.definitions.filter(d => d.lang === 'en').map((d, i) => (
                    <li key={i} className="flex gap-2 text-sm">
                      <span className="font-semibold text-gray-400">{i + 1}.</span>
                      <span className="text-gray-700">{d.meaning}</span>
                    </li>
                  ))}
                </ol>
              </div>
            )}

            {/* Examples */}
            {wordModal.examples?.length > 0 && (
              <div className="mb-4">
                <p className="mb-2 text-xs font-bold uppercase tracking-widest text-gray-400">Examples</p>
                <ul className="space-y-2">
                  {wordModal.examples.slice(0, 3).map((ex, i) => (
                    <li key={i} className="border-l-2 border-blue-200 pl-3">
                      <p className="text-sm italic text-gray-600">"{ex.sentence}"</p>
                      {ex.translation && (
                        <p className="text-xs text-gray-400 mt-0.5">{ex.translation}</p>
                      )}
                    </li>
                  ))}
                </ul>
              </div>
            )}

            {/* Collocations */}
            {wordModal.collocations?.length > 0 && (
              <div className="mb-4">
                <p className="mb-2 text-xs font-bold uppercase tracking-widest text-gray-400">Collocations</p>
                <div className="flex flex-wrap gap-1.5">
                  {wordModal.collocations.map((c, i) => (
                    <span key={i} className="rounded-full bg-gray-100 px-2.5 py-1 text-xs text-gray-600">{c}</span>
                  ))}
                </div>
              </div>
            )}

            {/* Save button */}
            <div className="border-t border-gray-100 pt-4">
              {wordModal.isSavedByUser ? (
                <Button
                  variant="secondary"
                  className="w-full"
                  onClick={async () => {
                    if (wordModal.userVocabularyId) {
                      await vocabularyApi.delete(wordModal.userVocabularyId)
                      setWordModal({ ...wordModal, isSavedByUser: false, userVocabularyId: '' })
                    }
                  }}
                >
                  ✓ Saved — click to remove
                </Button>
              ) : (
                <Button
                  className="w-full"
                  onClick={async () => {
                    const res = await vocabularyApi.save([wordModal.id], id)
                    if (res.data.data[0]) {
                      setWordModal({ ...wordModal, isSavedByUser: true, userVocabularyId: res.data.data[0].id })
                    }
                  }}
                >
                  <BookmarkPlus className="h-4 w-4" />
                  Save to vocabulary
                </Button>
              )}
            </div>
          </div>
        ) : null}
      </Modal>

      {/* Save Popup after leaving article */}
      <Modal
        open={savePopup.open}
        onClose={() => setSavePopup(prev => ({ ...prev, open: false }))}
        title="Save words to vocabulary?"
        maxWidth="max-w-sm"
      >
        <p className="mb-3 text-sm text-gray-500">Select words you looked up to add to your vocabulary:</p>
        <div className="space-y-2 mb-4">
          {savePopup.words.map(w => (
            <label key={w.wordId} className="flex items-center gap-3 rounded-lg p-2 hover:bg-gray-50 cursor-pointer">
              <input
                type="checkbox"
                checked={savePopup.selectedIds.has(w.wordId)}
                onChange={(e) => {
                  const next = new Set(savePopup.selectedIds)
                  if (e.target.checked) next.add(w.wordId)
                  else next.delete(w.wordId)
                  setSavePopup(prev => ({ ...prev, selectedIds: next }))
                }}
                className="h-4 w-4 rounded text-blue-600"
              />
              <span className="text-sm font-medium text-gray-800">{w.word}</span>
              <span className="text-xs text-gray-400">{w.partOfSpeech}</span>
            </label>
          ))}
        </div>
        <div className="flex gap-2">
          <Button
            variant="secondary"
            className="flex-1"
            onClick={() => setSavePopup(prev => ({ ...prev, open: false }))}
          >
            Skip
          </Button>
          <Button
            className="flex-1"
            loading={saveWords.isPending}
            disabled={savePopup.selectedIds.size === 0}
            onClick={() => saveWords.mutate()}
          >
            Save {savePopup.selectedIds.size} word{savePopup.selectedIds.size !== 1 ? 's' : ''}
          </Button>
        </div>
      </Modal>
    </div>
  )
}

import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, Edit2, Trash2, X, Check } from 'lucide-react'
import { adminApi } from '../api/admin'
import type { AdminArticle } from '../api/admin'
import { articlesApi } from '../api/articles'
import type { Topic } from '../api/articles'
import { Button } from '../components/ui/Button'
import { Spinner } from '../components/ui/Spinner'

const LEVELS = ['A1', 'A2', 'B1', 'B2', 'C1', 'C2']

interface ArticleForm {
  topicId: string
  title: string
  content: string
  level: string
  sourceUrl: string
}

const emptyForm: ArticleForm = { topicId: '', title: '', content: '', level: 'B1', sourceUrl: '' }

export function AdminPage() {
  const qc = useQueryClient()
  const [page, setPage] = useState(0)
  const [showModal, setShowModal] = useState(false)
  const [editingId, setEditingId] = useState<string | null>(null)
  const [form, setForm] = useState<ArticleForm>(emptyForm)
  const [deleteConfirmId, setDeleteConfirmId] = useState<string | null>(null)

  const { data: topicsData } = useQuery({
    queryKey: ['topics'],
    queryFn: () => articlesApi.getTopics().then(r => r.data.data),
  })
  const topics: Topic[] = topicsData ?? []

  const { data, isLoading } = useQuery({
    queryKey: ['admin-articles', page],
    queryFn: () => adminApi.getArticles({ page, size: 20 }).then(r => r.data.data),
  })

  const createMutation = useMutation({
    mutationFn: (d: ArticleForm) => adminApi.createArticle({ ...d }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['admin-articles'] }); closeModal() },
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, d }: { id: string; d: Partial<ArticleForm> }) => adminApi.updateArticle(id, d),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['admin-articles'] }); closeModal() },
  })

  const deleteMutation = useMutation({
    mutationFn: (id: string) => adminApi.deleteArticle(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['admin-articles'] }); setDeleteConfirmId(null) },
  })

  function openCreate() {
    setEditingId(null)
    setForm(emptyForm)
    setShowModal(true)
  }

  function openEdit(a: AdminArticle) {
    setEditingId(a.id)
    setForm({ topicId: a.topicId ?? '', title: a.title, content: a.content, level: a.languageLevel ?? 'B1', sourceUrl: a.sourceUrl ?? '' })
    setShowModal(true)
  }

  function closeModal() {
    setShowModal(false)
    setEditingId(null)
    setForm(emptyForm)
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (editingId) {
      updateMutation.mutate({ id: editingId, d: form })
    } else {
      createMutation.mutate(form)
    }
  }

  const articles: AdminArticle[] = data?.content ?? []
  const totalPages = data?.totalPages ?? 1
  const isPending = createMutation.isPending || updateMutation.isPending

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Admin — Articles</h1>
          <p className="mt-1 text-sm text-gray-500">Manage articles visible to users</p>
        </div>
        <Button onClick={openCreate}>
          <Plus className="mr-1 h-4 w-4" /> New Article
        </Button>
      </div>

      {isLoading ? (
        <div className="flex justify-center py-12"><Spinner /></div>
      ) : (
        <div className="overflow-x-auto rounded-xl border border-gray-200 bg-white">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-left text-xs font-semibold uppercase tracking-wide text-gray-500">
              <tr>
                <th className="px-4 py-3">Title</th>
                <th className="px-4 py-3">Topic</th>
                <th className="px-4 py-3">Level</th>
                <th className="px-4 py-3">Words</th>
                <th className="px-4 py-3">Published</th>
                <th className="px-4 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {articles.map(a => (
                <tr key={a.id} className="hover:bg-gray-50">
                  <td className="max-w-xs truncate px-4 py-3 font-medium text-gray-900">{a.title}</td>
                  <td className="px-4 py-3 text-gray-600">{a.topicName ?? '—'}</td>
                  <td className="px-4 py-3">
                    <span className="rounded-full bg-blue-100 px-2 py-0.5 text-xs font-medium text-blue-700">{a.languageLevel}</span>
                  </td>
                  <td className="px-4 py-3 text-gray-600">{a.wordCount}</td>
                  <td className="px-4 py-3 text-gray-500">
                    {a.publishedAt ? new Date(a.publishedAt).toLocaleDateString() : '—'}
                  </td>
                  <td className="px-4 py-3 text-right">
                    {deleteConfirmId === a.id ? (
                      <span className="inline-flex items-center gap-2">
                        <span className="text-xs text-red-600">Delete?</span>
                        <button
                          onClick={() => deleteMutation.mutate(a.id)}
                          className="rounded p-1 text-red-600 hover:bg-red-50"
                          disabled={deleteMutation.isPending}
                        >
                          <Check className="h-4 w-4" />
                        </button>
                        <button onClick={() => setDeleteConfirmId(null)} className="rounded p-1 text-gray-400 hover:bg-gray-100">
                          <X className="h-4 w-4" />
                        </button>
                      </span>
                    ) : (
                      <span className="inline-flex gap-2">
                        <button onClick={() => openEdit(a)} className="rounded p-1 text-gray-400 hover:bg-gray-100 hover:text-blue-600">
                          <Edit2 className="h-4 w-4" />
                        </button>
                        <button onClick={() => setDeleteConfirmId(a.id)} className="rounded p-1 text-gray-400 hover:bg-gray-100 hover:text-red-600">
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </span>
                    )}
                  </td>
                </tr>
              ))}
              {articles.length === 0 && (
                <tr><td colSpan={6} className="px-4 py-8 text-center text-gray-400">No articles yet</td></tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-2">
          <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0} className="rounded px-3 py-1 text-sm disabled:opacity-40 hover:bg-gray-100">Prev</button>
          <span className="text-sm text-gray-600">{page + 1} / {totalPages}</span>
          <button onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} disabled={page >= totalPages - 1} className="rounded px-3 py-1 text-sm disabled:opacity-40 hover:bg-gray-100">Next</button>
        </div>
      )}

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <div className="w-full max-w-2xl rounded-2xl bg-white p-6 shadow-xl">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-lg font-semibold">{editingId ? 'Edit Article' : 'New Article'}</h2>
              <button onClick={closeModal} className="rounded p-1 hover:bg-gray-100"><X className="h-5 w-5" /></button>
            </div>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="mb-1 block text-sm font-medium text-gray-700">Title *</label>
                <input
                  required
                  value={form.title}
                  onChange={e => setForm(f => ({ ...f, title: e.target.value }))}
                  className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
                  placeholder="Article title"
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="mb-1 block text-sm font-medium text-gray-700">Topic *</label>
                  <select
                    required
                    value={form.topicId}
                    onChange={e => setForm(f => ({ ...f, topicId: e.target.value }))}
                    className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
                  >
                    <option value="">Select topic</option>
                    {topics.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
                  </select>
                </div>
                <div>
                  <label className="mb-1 block text-sm font-medium text-gray-700">Level *</label>
                  <select
                    value={form.level}
                    onChange={e => setForm(f => ({ ...f, level: e.target.value }))}
                    className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
                  >
                    {LEVELS.map(l => <option key={l} value={l}>{l}</option>)}
                  </select>
                </div>
              </div>
              <div>
                <label className="mb-1 block text-sm font-medium text-gray-700">Content *</label>
                <textarea
                  required
                  rows={8}
                  value={form.content}
                  onChange={e => setForm(f => ({ ...f, content: e.target.value }))}
                  className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
                  placeholder="Article content..."
                />
              </div>
              <div>
                <label className="mb-1 block text-sm font-medium text-gray-700">Source URL</label>
                <input
                  value={form.sourceUrl}
                  onChange={e => setForm(f => ({ ...f, sourceUrl: e.target.value }))}
                  className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
                  placeholder="https://..."
                />
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <Button variant="secondary" type="button" onClick={closeModal}>Cancel</Button>
                <Button type="submit" disabled={isPending}>
                  {isPending ? <Spinner className="h-4 w-4" /> : editingId ? 'Save Changes' : 'Create Article'}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}

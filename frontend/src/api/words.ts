import api from './axios'

export interface Definition {
  lang: 'vi' | 'en'
  meaning: string
}

export interface Example {
  sentence: string
  translation: string
}

export interface WordTooltip {
  id: string
  word: string
  phonetic: string
  audioUrl: string
  partOfSpeech: string
  vnMeaning: string
  definitions: Definition[]
}

export interface WordDetail extends WordTooltip {
  examples: Example[]
  collocations: string[]
  isSavedByUser: boolean
  userVocabularyId: string
}

export const wordsApi = {
  getTooltip: (word: string) =>
    api.get<{ success: boolean; data: WordTooltip }>(`/words/${encodeURIComponent(word)}/tooltip`),

  getDetail: (word: string) =>
    api.get<{ success: boolean; data: WordDetail }>(`/words/${encodeURIComponent(word)}/detail`),
}

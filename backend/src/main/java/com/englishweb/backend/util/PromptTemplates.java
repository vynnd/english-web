package com.englishweb.backend.util;

/**
 * Single source of truth for all Claude AI prompts used in the application.
 *
 * Each method documents:
 *  - Purpose
 *  - Expected return format
 *  - Example output
 */
public final class PromptTemplates {

    private PromptTemplates() {}

    /**
     * Asks Claude to translate an English word and return its IPA + part of speech.
     *
     * <p><b>Return format:</b> Exactly 3 lines, no labels, no extra text:</p>
     * <pre>
     * line 1 — Vietnamese meaning  (1-4 words, as in a dictionary)
     * line 2 — IPA pronunciation   (e.g. /tɔːk/)
     * line 3 — Part of speech      (in English, e.g. verb / noun / adjective)
     * </pre>
     *
     * <p><b>Example output for "talk":</b>
     * <pre>
     * nói chuyện
     * /tɔːk/
     * verb
     * </pre>
     * </p>
     *
     * @param word the English word to look up
     */
    public static String vietnameseTranslation(String word) {
        return String.format(
                "Dịch từ \"%s\" từ tiếng Anh sang tiếng Việt và trả về đúng 3 dòng theo định dạng sau, không thêm bất kỳ chữ nào khác:\n\n" +
                "{nghĩa tiếng Việt}\n" +
                "{phát âm IPA}\n" +
                "{loại từ bằng tiếng Anh}",
                word
        );
    }
}

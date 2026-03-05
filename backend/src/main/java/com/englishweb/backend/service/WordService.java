package com.englishweb.backend.service;

import com.englishweb.backend.entity.UserVocabulary;
import com.englishweb.backend.entity.Word;
import com.englishweb.backend.util.PromptTemplates;
import com.englishweb.backend.exception.ResourceNotFoundException;
import com.englishweb.backend.repository.UserVocabularyRepository;
import com.englishweb.backend.repository.WordRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
public class WordService {

    private static final Logger log = LoggerFactory.getLogger(WordService.class);

    private final WordRepository wordRepository;
    private final UserVocabularyRepository userVocabularyRepository;
    private final ObjectMapper objectMapper;
    private final RestClient dictionaryClient;
    private final RestClient anthropicClient;

    private final String anthropicModel;

    @Autowired
    public WordService(WordRepository wordRepository,
                       UserVocabularyRepository userVocabularyRepository,
                       ObjectMapper objectMapper,
                       @Value("${anthropic.api.key}") String anthropicApiKey,
                       @Value("${anthropic.model}") String anthropicModel) {
        this.anthropicModel = anthropicModel;
        this.wordRepository = wordRepository;
        this.userVocabularyRepository = userVocabularyRepository;
        this.objectMapper = objectMapper;
        this.dictionaryClient = RestClient.builder()
                .baseUrl("https://api.dictionaryapi.dev/api/v2/entries/en")
                .build();
        this.anthropicClient = RestClient.builder()
                .baseUrl("https://api.anthropic.com/v1")
                .defaultHeader("x-api-key", anthropicApiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("content-type", "application/json")
                .build();
    }

    public Word getWordByText(String wordText) {
        return wordRepository.findByWordIgnoreCase(wordText)
                .map(this::ensureVietnamese)
                .orElseGet(() -> fetchAndSave(wordText));
    }

    /** If the cached word has no Vietnamese meaning, call Claude and patch it in-place. */
    @Transactional
    private Word ensureVietnamese(Word w) {
        if (w.getVnMeaning() != null && !w.getVnMeaning().isBlank()) return w;
        try {
            ClaudeResult claude = callClaude(w.getWord());
            if (!claude.viMeaning().isBlank()) w.setVnMeaning(claude.viMeaning());
            if ((w.getPhonetic() == null || w.getPhonetic().isBlank()) && !claude.phonetic().isBlank())
                w.setPhonetic(claude.phonetic());
            if ((w.getPartOfSpeech() == null || w.getPartOfSpeech().isBlank()) && !claude.partOfSpeech().isBlank())
                w.setPartOfSpeech(claude.partOfSpeech());
            return wordRepository.save(w);
        } catch (Exception e) {
            log.error("[ensureVietnamese] failed for '{}': {}", w.getWord(), e.getMessage());
            return w;
        }
    }

    private record ClaudeResult(String viMeaning, String phonetic, String partOfSpeech) {}

    @Transactional
    private Word fetchAndSave(String wordText) {
        try {
            // Step 1: Fetch from free dictionary API
            String dictResponse = dictionaryClient.get()
                    .uri("/{word}", wordText.toLowerCase())
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(dictResponse);
            if (!root.isArray() || root.size() == 0)
                throw new ResourceNotFoundException("Word not found: " + wordText);

            JsonNode entry = root.get(0);

            // Phonetic + audio URL
            String phonetic = "";
            String audioUrl = "";
            if (entry.has("phonetic") && !entry.get("phonetic").asText().isBlank()) {
                phonetic = entry.get("phonetic").asText();
            }
            if (entry.has("phonetics")) {
                for (JsonNode ph : entry.get("phonetics")) {
                    if (phonetic.isBlank() && ph.has("text") && !ph.get("text").asText().isBlank()) {
                        phonetic = ph.get("text").asText();
                    }
                    if (audioUrl.isBlank() && ph.has("audio") && !ph.get("audio").asText().isBlank()) {
                        audioUrl = ph.get("audio").asText();
                    }
                }
            }

            // Part of speech, English definitions, examples
            String partOfSpeech = "";
            List<Map<String, String>> definitions = new ArrayList<>();
            List<Map<String, String>> examples = new ArrayList<>();

            if (entry.has("meanings") && entry.get("meanings").size() > 0) {
                partOfSpeech = entry.get("meanings").get(0).path("partOfSpeech").asText("");
                for (JsonNode meaning : entry.get("meanings")) {
                    for (JsonNode def : meaning.path("definitions")) {
                        String defText = def.path("definition").asText("");
                        if (!defText.isBlank()) {
                            definitions.add(Map.of("lang", "en", "meaning", defText));
                        }
                        String ex = def.path("example").asText("");
                        if (!ex.isBlank()) {
                            Map<String, String> exMap = new HashMap<>();
                            exMap.put("sentence", ex);
                            exMap.put("translation", "");
                            examples.add(exMap);
                        }
                    }
                }
            }

            // Step 2: Call Claude — Vietnamese meaning + IPA/pos fallbacks
            ClaudeResult claude = callClaude(wordText);
            if (phonetic.isBlank() && !claude.phonetic().isBlank()) phonetic = claude.phonetic();
            if (partOfSpeech.isBlank() && !claude.partOfSpeech().isBlank()) partOfSpeech = claude.partOfSpeech();

            Word word = Word.builder()
                    .word(wordText.toLowerCase())
                    .phonetic(phonetic)
                    .audioUrl(audioUrl)
                    .partOfSpeech(partOfSpeech)
                    .vnMeaning(claude.viMeaning().isBlank() ? null : claude.viMeaning())
                    .definitions(objectMapper.writeValueAsString(definitions))
                    .examples(objectMapper.writeValueAsString(examples))
                    .collocations("[]")
                    .build();
            return wordRepository.save(word);

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ResourceNotFoundException("Word not found: " + wordText);
        }
    }

    private ClaudeResult callClaude(String wordText) {
        try {
            String prompt = PromptTemplates.vietnameseTranslation(wordText);

            Map<String, Object> body = Map.of(
                    "model", anthropicModel,
                    "max_tokens", 150,
                    "messages", List.of(Map.of("role", "user", "content", prompt))
            );

            String response = anthropicClient.post()
                    .uri("/messages")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(response);
            String text = node.path("content").get(0).path("text").asText().trim();

            // Filter blank lines so stray leading/trailing empty lines don't shift positions
            String[] lines = java.util.Arrays.stream(text.split("\\r?\\n"))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toArray(String[]::new);

            String viMeaning    = lines.length > 0 ? lines[0] : "";
            String phonetic     = lines.length > 1 ? lines[1] : "";
            String partOfSpeech = lines.length > 2 ? lines[2] : "";

            log.info("[Claude] word='{}' raw='{}'", wordText, text);
            log.info("[Claude] parsed vi='{}' ipa='{}' pos='{}'", viMeaning, phonetic, partOfSpeech);
            return new ClaudeResult(viMeaning, phonetic, partOfSpeech);
        } catch (Exception e) {
            log.error("[Claude] failed for '{}': {}", wordText, e.getMessage());
            return new ClaudeResult("", "", "");
        }
    }

    public boolean isSavedByUser(UUID userId, UUID wordId) {
        return userVocabularyRepository.existsByUserIdAndWordId(userId, wordId);
    }

    public UUID getUserVocabularyId(UUID userId, UUID wordId) {
        return userVocabularyRepository.findByUserIdAndWordId(userId, wordId)
                .map(UserVocabulary::getId)
                .orElse(null);
    }

    public Object parseJson(String json) {
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception e) {
            return List.of();
        }
    }
}

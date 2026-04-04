package com.example.bossbot.chat.service;

import com.example.bossbot.bannedword.entity.BannedWord;
import com.example.bossbot.bannedword.repository.BannedWordRepository;
import com.example.bossbot.chat.dto.ModerationResult;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContentModerationServiceImpl implements ContentModerationService {

    private final BannedWordRepository bannedWordRepository;
    private final AtomicReference<List<BannedWord>> cachedBannedWords = new AtomicReference<>(List.of());

    @PostConstruct
    void init() {
        refreshCache();
    }

    @Transactional(readOnly = true)
    public void refreshCache() {
        cachedBannedWords.set(bannedWordRepository.findByIsActiveTrue());
        log.info("Banned words cache refreshed ({} entries)", cachedBannedWords.get().size());
    }

    @Override
    public ModerationResult check(String content) {
        List<BannedWord> activeBannedWords = cachedBannedWords.get();
        String lowerContent = content.toLowerCase();

        ModerationResult.Severity highestSeverity = ModerationResult.Severity.NONE;
        String matchedWord = null;

        for (BannedWord bannedWord : activeBannedWords) {
            String word = bannedWord.getWord().toLowerCase();
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(word) + "\\b", Pattern.CASE_INSENSITIVE);

            if (pattern.matcher(lowerContent).find()) {
                ModerationResult.Severity severity = parseSeverity(bannedWord.getSeverity());

                if (severity.ordinal() > highestSeverity.ordinal()) {
                    highestSeverity = severity;
                    matchedWord = bannedWord.getWord();
                }

                if (highestSeverity == ModerationResult.Severity.HIGH) {
                    break;
                }
            }
        }

        return ModerationResult.of(highestSeverity, matchedWord);
    }

    private ModerationResult.Severity parseSeverity(String severity) {
        if (severity == null) {
            return ModerationResult.Severity.LOW;
        }

        return switch (severity.toUpperCase()) {
            case "HIGH" -> ModerationResult.Severity.HIGH;
            default -> ModerationResult.Severity.LOW;
        };
    }
}

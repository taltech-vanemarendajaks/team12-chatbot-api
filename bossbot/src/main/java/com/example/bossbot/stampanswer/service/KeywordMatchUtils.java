package com.example.bossbot.stampanswer.service;

import com.example.bossbot.stampanswer.entity.StampAnswer;

import java.util.*;
import java.util.Locale;

final class KeywordMatchUtils {

    private KeywordMatchUtils() {
    }

    static Optional<StampAnswer> findBestKeywordMatch(String input, List<StampAnswer> stampAnswers, int minInputLength, double keywordThreshold) {
        if (input == null || input.trim().length() < minInputLength) {
            return Optional.empty();
        }

        Set<String> inputWords = tokenize(input);
        if (inputWords.isEmpty()) {
            return Optional.empty();
        }

        StampAnswer bestMatch = null;
        double bestScore = 0;

        for (StampAnswer sa : stampAnswers) {
            Set<String> answerWords = tokenize(sa.getQuestion());
            if (sa.getKeywords() != null && !sa.getKeywords().isBlank()) {
                for (String keyword : sa.getKeywords().split(",")) {
                    answerWords.addAll(tokenize(keyword.trim()));
                }
            }

            long matchCount = inputWords.stream().filter(answerWords::contains).count();
            // Jaccard similarity: intersection / union — fair to both short and long inputs
            long unionSize = inputWords.size() + answerWords.size() - matchCount;
            double score = unionSize == 0 ? 0.0 : (double) matchCount / unionSize;

            if (score > bestScore || (score == bestScore && bestMatch != null)) {
                bestScore = score;
                bestMatch = sa;
            }
        }

        if (bestScore >= keywordThreshold) {
            return Optional.of(bestMatch);
        }
        return Optional.empty();
    }

    private static Set<String> tokenize(String text) {
        Set<String> words = new HashSet<>();
        for (String token : text.toLowerCase(Locale.ROOT).split("\\W+")) {
            if (token.length() >= 2) {
                words.add(token);
            }
        }
        return words;
    }
}

package com.example.bossbot.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModerationResult {

    public enum Severity {
        NONE, LOW, HIGH
    }

    private Severity severity;
    private String matchedWord;

    public static ModerationResult none() {
        return new ModerationResult(Severity.NONE, null);
    }

    public static ModerationResult of(Severity severity, String matchedWord) {
        return new ModerationResult(severity, matchedWord);
    }
}

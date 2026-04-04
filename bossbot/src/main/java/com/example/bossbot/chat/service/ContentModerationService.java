package com.example.bossbot.chat.service;

import com.example.bossbot.chat.dto.ModerationResult;

public interface ContentModerationService {

    ModerationResult check(String content);

    void refreshCache();
}

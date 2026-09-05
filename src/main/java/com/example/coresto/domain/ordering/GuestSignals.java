package com.example.coresto.domain.ordering;

public record GuestSignals(
    String publicToken,
    String phoneE164,
    String emailNormalized,
    String userAgent,
    String language,
    String timeZone,
    String screen,
    String clientFingerprint
) {}

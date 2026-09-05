package com.example.coresto.domain.ordering;

public interface GuestIdentityService {
    /**
     * Resolve or create a GuestDevice from the given signals. Resolution order:
     * 1. publicToken matches → update lastSeenAt, return existing
     * 2. phoneE164 or emailNormalized matches → reuse, set/overwrite publicToken
     * 3. fingerprintHash single match → optional weak reuse (MVP-skippable)
     * 4. INSERT new GuestDevice; generate publicToken = UUID; return
     */
    GuestDevice resolveOrCreate(GuestSignals signals);
}

package com.example.coresto.domain.ordering;

import com.example.coresto.repository.GuestDeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GuestIdentityServiceImpl implements GuestIdentityService {

    private final GuestDeviceRepository guestDeviceRepository;

    @Value("${coresto.guest.id-secret:default-dev-secret}")
    private String guestIdSecret;

    @Override
    @Transactional
    public GuestDevice resolveOrCreate(GuestSignals signals) {
        // 1. publicToken match
        if (signals.publicToken() != null) {
            Optional<GuestDevice> byToken = guestDeviceRepository.findByPublicToken(signals.publicToken());
            if (byToken.isPresent()) {
                GuestDevice device = byToken.get();
                device.setLastSeenAt(Instant.now());
                return device;
            }
        }

        // 2. phone or email match
        if (signals.phoneE164() != null) {
            Optional<GuestDevice> byPhone = guestDeviceRepository.findByPhoneE164(signals.phoneE164());
            if (byPhone.isPresent()) {
                GuestDevice device = byPhone.get();
                if (signals.publicToken() != null) device.setPublicToken(signals.publicToken());
                device.setLastSeenAt(Instant.now());
                return device;
            }
        }
        if (signals.emailNormalized() != null) {
            Optional<GuestDevice> byEmail = guestDeviceRepository.findByEmailNormalized(signals.emailNormalized());
            if (byEmail.isPresent()) {
                GuestDevice device = byEmail.get();
                if (signals.publicToken() != null) device.setPublicToken(signals.publicToken());
                device.setLastSeenAt(Instant.now());
                return device;
            }
        }

        // 3. fingerprintHash weak match (skippable in MVP)

        // 4. Insert new
        GuestDevice device = new GuestDevice();
        device.setPublicToken(signals.publicToken() != null ? signals.publicToken() : UUID.randomUUID().toString());
        device.setPhoneE164(signals.phoneE164());
        device.setEmailNormalized(signals.emailNormalized());
        device.setFingerprintHash(signals.clientFingerprint() != null ? hmac("fingerprint:" + signals.clientFingerprint()) : null);
        device.setUserAgent(signals.userAgent());
        device.setLanguage(signals.language());
        device.setLastSeenAt(Instant.now());

        return guestDeviceRepository.save(device);
    }

    private String hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(guestIdSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("HMAC computation failed", e);
        }
    }
}

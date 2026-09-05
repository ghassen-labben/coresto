package com.example.coresto.domain.ordering;

import com.example.coresto.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * No FK to User. Browsers cannot read IMEI. publicToken is the primary business key stored in browser localStorage.
 * Frontend contract: persist publicToken in localStorage, send on every /public request, same token = same GuestDevice = same diner.
 */
@Entity
@Table(
    name = "guest_devices",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "public_token"),
        @UniqueConstraint(columnNames = "phone_e164"),
        @UniqueConstraint(columnNames = "email_normalized")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class GuestDevice extends BaseEntity {

    @Id
    @UuidGenerator
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "public_token", nullable = false)
    private String publicToken;

    @Column(name = "phone_e164")
    private String phoneE164;

    @Column(name = "email_normalized")
    private String emailNormalized;

    @Column(name = "fingerprint_hash")
    private String fingerprintHash;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "language")
    private String language;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;
}

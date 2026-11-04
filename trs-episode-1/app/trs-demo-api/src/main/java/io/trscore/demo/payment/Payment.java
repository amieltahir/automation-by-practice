package io.trscore.demo.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Payment(
        UUID id,
        BigDecimal amount,
        String currency,
        String description,
        PaymentStatus status,
        Instant createdAt
) {}

package io.trscore.demo.payment;

import java.math.BigDecimal;

public record PaymentRequest(
        BigDecimal amount,
        String currency,
        String description
) {}

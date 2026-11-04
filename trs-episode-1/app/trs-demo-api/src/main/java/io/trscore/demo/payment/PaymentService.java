
package io.trscore.demo.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final Map<UUID, Payment> payments = new ConcurrentHashMap<>();

    private final Predicate<Payment> processor;

    public PaymentService() {
        // Simulated gateway: approves every payment.
        this(payment -> true);
    }

    PaymentService(Predicate<Payment> processor) {
        this.processor = processor;

        create(new PaymentRequest(
                new BigDecimal("125.50"),
                "CAD",
                "Invoice INV-1001"
        ));

        create(new PaymentRequest(
                new BigDecimal("850.00"),
                "CAD",
                "Supplier payment"
        ));
    }

    public List<Payment> findAll() {
        return List.copyOf(payments.values());
    }

    public Payment findById(UUID id) {
        return payments.get(id);
    }

    public Payment create(PaymentRequest request) {
        if (request.amount() == null || request.amount().signum() <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be positive"
            );
        }

        if (request.currency() == null
                || !request.currency().matches("[A-Z]{3}")) {
            throw new IllegalArgumentException(
                    "Currency must be a three-letter uppercase code"
            );
        }

        if (request.description() == null
                || request.description().isBlank()) {
            throw new IllegalArgumentException(
                    "Payment description is required"
            );
        }

        Payment payment = new Payment(
                UUID.randomUUID(),
                request.amount(),
                request.currency(),
                request.description(),
                PaymentStatus.PENDING,
                Instant.now()
        );

        payments.put(payment.id(), payment);

        return process(payment.id());
    }

    /**
     * Moves a payment PENDING -> PROCESSING -> COMPLETED (or FAILED) and
     * returns the stored, updated payment.
     */
    public Payment process(UUID id) {
        // Atomic claim: only one caller can move a payment out of PENDING.
        payments.compute(id, (key, current) -> {
            if (current == null) {
                throw new PaymentNotFoundException(id);
            }
            if (current.status() != PaymentStatus.PENDING) {
                throw new PaymentStateException(id, current.status());
            }
            return withStatus(current, PaymentStatus.PROCESSING);
        });

        boolean approved;
        try {
            approved = processor.test(payments.get(id));
        } catch (RuntimeException exception) {
            approved = false;
        }

        PaymentStatus result = approved
                ? PaymentStatus.COMPLETED
                : PaymentStatus.FAILED;

        return payments.computeIfPresent(
                id, (key, current) -> withStatus(current, result));
    }

    private static Payment withStatus(Payment payment, PaymentStatus status) {
        return new Payment(
                payment.id(),
                payment.amount(),
                payment.currency(),
                payment.description(),
                status,
                payment.createdAt()
        );
    }

    public static class PaymentNotFoundException extends RuntimeException {

        public PaymentNotFoundException(UUID id) {
            super("Payment not found: " + id);
        }
    }

    public static class PaymentStateException extends RuntimeException {

        public PaymentStateException(UUID id, PaymentStatus status) {
            super("Payment " + id + " cannot be processed in status " + status);
        }
    }
}

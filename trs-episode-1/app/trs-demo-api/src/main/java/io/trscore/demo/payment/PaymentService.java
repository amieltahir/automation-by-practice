
package io.trscore.demo.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final Map<UUID, Payment> payments = new ConcurrentHashMap<>();

    public PaymentService() {
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

        // Save the payment initially as PENDING.
        payments.put(payment.id(), payment);

        // Simulate automatic successful payment processing.
        Payment completedPayment = new Payment(
                payment.id(),
                payment.amount(),
                payment.currency(),
                payment.description(),
                PaymentStatus.COMPLETED,
                payment.createdAt()
        );

        payments.put(completedPayment.id(), completedPayment);

        return completedPayment;
    }

    public static class PaymentNotFoundException extends RuntimeException {

        public PaymentNotFoundException(UUID id) {
            super("Payment not found: " + id);
        }
    }
}



package io.trscore.demo.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentServiceTest {

    private PaymentService service;

    @BeforeEach
    void setUp() {
        service = new PaymentService();
    }

    @Test
    void returnsSamplePayments() {
        assertEquals(2, service.findAll().size());
    }

    @Test
    void createsAndAutomaticallyProcessesPayment() {
        Payment created = service.create(
                new PaymentRequest(
                        new BigDecimal("500.00"),
                        "USD",
                        "Processing test"
                )
        );

        assertNotNull(created.id());
        assertEquals(new BigDecimal("500.00"), created.amount());
        assertEquals("USD", created.currency());
        assertEquals("Processing test", created.description());
        assertEquals(PaymentStatus.COMPLETED, created.status());

        Payment retrieved = service.findById(created.id());

        assertNotNull(retrieved);
        assertEquals(PaymentStatus.COMPLETED, retrieved.status());
    }

    @Test
    void rejectsZeroAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(
                        new PaymentRequest(
                                BigDecimal.ZERO,
                                "CAD",
                                "Invalid payment"
                        )
                )
        );
    }

    @Test
    void returnsNullForUnknownPayment() {
        assertEquals(null, service.findById(UUID.randomUUID()));
    }
}



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

    @Test
    void failedProcessingIsRetainedAsFailed() {
        PaymentService failing = new PaymentService(payment -> false);

        Payment created = failing.create(
                new PaymentRequest(new BigDecimal("10.00"), "CAD", "Declined"));

        assertEquals(PaymentStatus.FAILED, created.status());
        assertEquals(PaymentStatus.FAILED,
                failing.findById(created.id()).status());
    }

    @Test
    void processorExceptionMarksPaymentFailed() {
        PaymentService throwing = new PaymentService(payment -> {
            throw new IllegalStateException("gateway down");
        });

        Payment created = throwing.create(
                new PaymentRequest(new BigDecimal("10.00"), "CAD", "Error"));

        assertEquals(PaymentStatus.FAILED, created.status());
    }

    @Test
    void processorSeesProcessingStatus() {
        PaymentService[] holder = new PaymentService[1];
        PaymentStatus[] seen = new PaymentStatus[1];
        holder[0] = new PaymentService(payment -> {
            seen[0] = payment.status();
            return true;
        });

        holder[0].create(
                new PaymentRequest(new BigDecimal("1.00"), "CAD", "Observe"));

        assertEquals(PaymentStatus.PROCESSING, seen[0]);
    }

    @Test
    void cannotProcessTwice() {
        Payment completed = service.findAll().get(0);

        assertThrows(
                PaymentService.PaymentStateException.class,
                () -> service.process(completed.id()));
        assertEquals(PaymentStatus.COMPLETED,
                service.findById(completed.id()).status());
    }

    @Test
    void processingUnknownPaymentFails() {
        assertThrows(
                PaymentService.PaymentNotFoundException.class,
                () -> service.process(UUID.randomUUID()));
    }

    @Test
    void findAllReturnsLatestStatuses() {
        service.findAll().forEach(p ->
                assertEquals(PaymentStatus.COMPLETED, p.status()));
    }
}

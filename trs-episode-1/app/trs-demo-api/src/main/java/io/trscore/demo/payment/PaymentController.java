
package io.trscore.demo.payment;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public List<Payment> getPayments() {
        return paymentService.findAll();
    }

    @GetMapping("/{id}")
    public Payment getPayment(@PathVariable UUID id) {
        Payment payment = paymentService.findById(id);

        if (payment == null) {
            throw new PaymentService.PaymentNotFoundException(id);
        }

        return payment;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Payment createPayment(@RequestBody PaymentRequest request) {
        return paymentService.create(request);
    }

    @ExceptionHandler(PaymentService.PaymentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(
            PaymentService.PaymentNotFoundException exception) {
        return exception.getMessage();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(IllegalArgumentException exception) {
        return exception.getMessage();
    }
}


package personal.yejin.foodDelivery.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import personal.yejin.foodDelivery.domain.order.dto.OrderPaymentRequest;
import personal.yejin.foodDelivery.domain.payment.service.PaymentService;
import personal.yejin.foodDelivery.dto.PaymentResponse;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{orderId}")
    public ResponseEntity<PaymentResponse> processPayment(
            @PathVariable Long orderId,
            @RequestBody OrderPaymentRequest request
    ) {
        PaymentResponse response = paymentService.processPayment(orderId, request);
        return ResponseEntity.accepted().body(response);
    }
}

package personal.yejin.foodDelivery.domain.payment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import personal.yejin.foodDelivery.domain.order.dto.OrderPaymentRequest;
import personal.yejin.foodDelivery.domain.order.dto.OrderPaymentResponse;
import personal.yejin.foodDelivery.domain.payment.service.PaymentService;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{orderId}")
    public ResponseEntity<OrderPaymentResponse> processPayment(
            @PathVariable Long orderId,
            @PathVariable String correlatedId,
            @RequestBody OrderPaymentRequest request
    ) {
        OrderPaymentResponse response = paymentService.processPayment(orderId, correlatedId, request);
        return ResponseEntity.ok(response);
    }

}

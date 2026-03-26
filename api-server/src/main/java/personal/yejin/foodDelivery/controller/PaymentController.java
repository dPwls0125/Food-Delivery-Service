package personal.yejin.foodDelivery.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import personal.yejin.foodDelivery.domain.order.dto.OrderPaymentRequest;
import personal.yejin.foodDelivery.service.facade.PaymentFacade;
import personal.yejin.foodDelivery.dto.PaymentResponse;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentFacade paymentFacade;

    @PostMapping("/{orderId}")
    public ResponseEntity<PaymentResponse> processPayment(
            @PathVariable Long orderId,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestBody OrderPaymentRequest request) {
        PaymentResponse response = paymentFacade.processPayment(orderId, userId, request);
        return ResponseEntity.accepted().body(response);
    }
}

package personal.yejin.foodDelivery.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import personal.yejin.foodDelivery.client.StatusServerClient;
import personal.yejin.foodDelivery.domain.order.dto.OrderPaymentRequest;
import personal.yejin.foodDelivery.service.facade.PaymentFacade;
import personal.yejin.foodDelivery.dto.PaymentResponse;
import personal.yejin.foodDelivery.dto.PaymentStatusResponse;
import personal.yejin.model.PaymentStatus;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentFacade paymentFacade;
    private final StatusServerClient statusServerClient;

    @PostMapping("/{orderId}")
    public ResponseEntity<PaymentResponse> processPayment(
            @PathVariable Long orderId,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestBody OrderPaymentRequest request) {
        PaymentResponse response = paymentFacade.processPayment(orderId, userId, request);
        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/status/{orderId}")
    public ResponseEntity<PaymentStatusResponse> getPaymentStatus(@PathVariable Long orderId) {
        PaymentStatusResponse status = statusServerClient.getPaymentStatus(orderId);
        if (status == null) {
            return ResponseEntity.ok(new PaymentStatusResponse(orderId, PaymentStatus.PENDING, null, null, null));
        }
        return ResponseEntity.ok(status);
    }
}


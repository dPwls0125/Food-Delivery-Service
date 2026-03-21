package personal.yejin.foodDelivery.domain.payment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import personal.yejin.foodDelivery.domain.order.dto.OrderPaymentRequest;
import personal.yejin.foodDelivery.domain.order.dto.OrderPaymentResponse;
import personal.yejin.foodDelivery.domain.payment.dto.DiscountDetails;
import personal.yejin.foodDelivery.domain.payment.dto.DiscountPreviewRequest;
import personal.yejin.foodDelivery.domain.payment.dto.DiscountPreviewResponse;
import personal.yejin.foodDelivery.domain.payment.service.PaymentService;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{orderId}/preview")
    public ResponseEntity<DiscountPreviewResponse> previewDiscounts(
            @PathVariable Long orderId,
            @RequestBody DiscountPreviewRequest request
    ) {
        DiscountDetails discountDetails = new DiscountDetails(3000, 1500);
        DiscountPreviewResponse fakeResponse = new DiscountPreviewResponse(
                orderId,
                18000,
                discountDetails,
                4500,
                13500
        );
        return ResponseEntity.ok(fakeResponse);
    }

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

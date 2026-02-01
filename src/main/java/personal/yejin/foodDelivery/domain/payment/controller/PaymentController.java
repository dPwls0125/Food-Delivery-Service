package personal.yejin.foodDelivery.domain.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import personal.yejin.foodDelivery.domain.order.dto.OrderPaymentRequest;
import personal.yejin.foodDelivery.domain.order.dto.OrderPaymentResponse;
import personal.yejin.foodDelivery.domain.payment.dto.DiscountDetails;
import personal.yejin.foodDelivery.domain.payment.dto.DiscountPreviewRequest;
import personal.yejin.foodDelivery.domain.payment.dto.DiscountPreviewResponse;
import personal.yejin.foodDelivery.domain.order.dto.*;
import personal.yejin.foodDelivery.domain.order.model.OrderStatus;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
public class PaymentController {
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
            @RequestBody OrderPaymentRequest request
    ) {
        OrderPaymentResponse fakeResponse = new OrderPaymentResponse(
                orderId,
                OrderStatus.PAID,
                OrderPaymentResponse.PaymentStatus.SUCCESS,
                18000,
                4500,
                13500,
                request.paymentMethod(),
                LocalDateTime.parse("2026-01-05T15:10:00")
        );
        return ResponseEntity.ok(fakeResponse);
    }

}

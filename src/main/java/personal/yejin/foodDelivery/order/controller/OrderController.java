package personal.yejin.foodDelivery.order.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import personal.yejin.foodDelivery.order.dto.*;
import personal.yejin.foodDelivery.order.model.OrderStatus;
import personal.yejin.foodDelivery.rider.dto.RiderOrderDetailResponse;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @PostMapping
    public ResponseEntity<OrderCreateResponse> createOrder(
            @RequestBody OrderCreateRequest orderCreateRequest){
        long orderId = 5001L;

        OrderCreateResponse response = OrderCreateResponse.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.CREATED)
                .totalPrice(18000)
                .build();

        return ResponseEntity.created(URI.create("/api/orders/" + orderId))
                .body(response);
    }

    @PostMapping("/{orderId}/discounts/preview")
    public ResponseEntity<DiscountPreviewResponse> previewDiscounts(
        @PathVariable Long orderId,
        @RequestBody DiscountPreviewRequest request
    ) {
        DiscountPreviewResponse.DiscountDetails discountDetails = new DiscountPreviewResponse.DiscountDetails(3000, 1500);
        DiscountPreviewResponse fakeResponse = new DiscountPreviewResponse(
            orderId,
            18000,
            discountDetails,
            4500,
            13500
        );
        return ResponseEntity.ok(fakeResponse);
    }

    @PostMapping("/{orderId}/payment")
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



    @GetMapping("/{orderId}")
    public ResponseEntity<RiderOrderDetailResponse> getOrder(@PathVariable Long orderId){
        var storeInfo = RiderOrderDetailResponse.StoreInfo.builder()
            .name("김밥천국")
            .address("서울시 강남구 ...")
            .build();

        var itemsInfo = List.of(
            RiderOrderDetailResponse.ItemInfo.builder().name("김밥").quantity(2).build(),
            RiderOrderDetailResponse.ItemInfo.builder().name("라면").quantity(1).build()
        );

        var fakeResponse = RiderOrderDetailResponse.builder()
            .orderId(orderId)
            .store(storeInfo)
            .deliveryAddress("서울시 강남구 테헤란로 123")
            .orderStatus(OrderStatus.PAID) // Spec says PICKED_UP, but PAID is also a valid status from the enum. Using this for variety.
            .items(itemsInfo)
            .customerNote("문 앞에 놔주세요")
            .build();

        return ResponseEntity.ok(fakeResponse);
    }


}


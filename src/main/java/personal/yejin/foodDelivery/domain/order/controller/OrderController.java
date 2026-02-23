package personal.yejin.foodDelivery.domain.order.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import personal.yejin.foodDelivery.domain.order.dto.OrderCreateRequest;
import personal.yejin.foodDelivery.domain.order.dto.OrderCreateResponse;
import personal.yejin.foodDelivery.domain.order.model.OrderStatus;
import personal.yejin.foodDelivery.domain.payment.dto.DiscountDetails;
import personal.yejin.foodDelivery.domain.payment.dto.DiscountPreviewRequest;
import personal.yejin.foodDelivery.domain.payment.dto.DiscountPreviewResponse;
import personal.yejin.foodDelivery.domain.rider.dto.RiderOrderDetailResponse;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/orders")
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


    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable Long orderId){ // TODO : role에 따라서 응답 DTO 분기하기
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
            .orderStatus(OrderStatus.PAID)
            .items(itemsInfo)
            .customerNote("문 앞에 놔주세요")
            .build();

        return ResponseEntity.ok(fakeResponse);
    }

    @PostMapping("/{orderId}/discounts/preview")
    public ResponseEntity<DiscountPreviewResponse> getDiscountPreview(
            @PathVariable Long orderId,
            @RequestBody DiscountPreviewRequest discountPreviewRequest) {
        // Fake response based on API spec
        DiscountDetails discountDetails = new DiscountDetails(3000, 1500);
        DiscountPreviewResponse response = new DiscountPreviewResponse(
                orderId,
                18000,
                discountDetails,
                4500,
                13500
        );
        return ResponseEntity.ok(response);
    }



}


package personal.yejin.foodDelivery.order.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import personal.yejin.foodDelivery.order.dto.*;
import personal.yejin.foodDelivery.order.model.OrderStatus;
import personal.yejin.foodDelivery.rider.dto.RiderOrderDetailResponse;

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


}


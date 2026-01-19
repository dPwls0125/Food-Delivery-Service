package personal.yejin.foodDelivery.domain.order.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import personal.yejin.foodDelivery.domain.order.dto.OrderCreateRequest;
import personal.yejin.foodDelivery.domain.order.dto.OrderCreateResponse;
import personal.yejin.foodDelivery.domain.order.model.OrderStatus;
import personal.yejin.foodDelivery.domain.rider.dto.RiderOrderDetailResponse;

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


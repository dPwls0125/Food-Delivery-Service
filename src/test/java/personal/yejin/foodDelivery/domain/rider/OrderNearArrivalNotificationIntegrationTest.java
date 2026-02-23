package personal.yejin.foodDelivery.domain.rider;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import personal.yejin.foodDelivery.domain.delivery.dto.DispatchRequest;
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryType;
import personal.yejin.foodDelivery.domain.delivery.repository.DeliveryRepository;
import personal.yejin.foodDelivery.domain.order.model.Order;
import personal.yejin.foodDelivery.domain.order.model.OrderStatus;
import personal.yejin.foodDelivery.domain.order.repository.OrderRepository;
import personal.yejin.foodDelivery.domain.rider.dto.RiderLocationRequest;
import personal.yejin.foodDelivery.domain.rider.model.Location;
import personal.yejin.foodDelivery.domain.rider.model.Rider;
import personal.yejin.foodDelivery.domain.rider.model.RiderStatus;
import personal.yejin.foodDelivery.domain.rider.repository.RiderRepository;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderNearArrivalNotificationIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private RiderRepository riderRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Test
    @DisplayName("주문자 SSE 구독 후 라이더가 도착지 근접 시 near-arrival 알림을 수신한다")
    void customerReceivesNearArrivalNotification() {
        deliveryRepository.deleteAll();
        orderRepository.deleteAll();
        riderRepository.deleteAll();

        Rider rider = riderRepository.save(Rider.builder()
                .name("근접테스트 라이더")
                .status(RiderStatus.READY)
                .location(new Location(37.5000, 127.0000))
                .build());

        Order order = orderRepository.save(Order.builder()
                .storeId(10L)
                .pickupLocation(new Location(37.5005, 127.0005))
                .deliveryLocation(new Location(37.5200, 127.0200))
                .deliveryAddress("서울시 강남구")
                .orderStatus(OrderStatus.CREATED)
                .deliveryType(DeliveryType.SINGLE)
                .build());

        Long orderId = order.getId();

        Flux<String> orderNotificationStream = webTestClient.get()
                .uri("/orders/" + orderId + "/notifications/subscribe")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .returnResult(String.class)
                .getResponseBody();

        StepVerifier.create(orderNotificationStream)
                .assertNext(event -> {
                    log.info(">>> [ORDER SSE CONNECTED] {}", event);
                    assertThat(event).contains("orderId=" + orderId);
                })
                .then(() -> {
                    webTestClient.post()
                            .uri("/deliveries/" + orderId + "/dispatch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(new DispatchRequest(DeliveryType.SINGLE))
                            .exchange()
                            .expectStatus().isOk();
                })
                .then(() -> {
                    webTestClient.post()
                            .uri("/riders/" + rider.getId() + "/location")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(new RiderLocationRequest(37.5201, 127.0201))
                            .exchange()
                            .expectStatus().isCreated();
                })
                .assertNext(event -> {
                    log.info(">>> [ORDER NEAR ARRIVAL EVENT] {}", event);
                    assertThat(event).contains("\"orderId\":" + orderId);
                    assertThat(event).contains("\"riderId\":" + rider.getId());
                    assertThat(event).contains("delivery-near-arrival");
                })
                .thenCancel()
                .verify(Duration.ofSeconds(20));
    }
}

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
import personal.yejin.foodDelivery.domain.order.model.Order;
import personal.yejin.foodDelivery.domain.order.model.OrderStatus;
import personal.yejin.foodDelivery.domain.order.repository.OrderRepository;
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
public class RiderNotificationIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private RiderRepository riderRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("SSE 구독 연결 및 실시간 배차 알림 수신 통합 테스트")
    void testSseSubscriptionAndRealTimeNotification() {
        Rider rider = Rider.builder()
                .name("테스트라이더")
                .status(RiderStatus.READY)
                .location(new Location(37.5, 127.0))
                .build();
        rider = riderRepository.save(rider);
        Long riderId = rider.getId();

        Order order = Order.builder()
                .storeId(101L)
                .pickupLocation(new Location(37.51, 127.01))
                .deliveryLocation(new Location(37.52, 127.02))
                .deliveryAddress("서울시 강남구")
                .orderStatus(OrderStatus.CREATED)
                .build();
        order = orderRepository.save(order);
        Long orderId = order.getId();

        log.info(">>> [SETUP] riderId={}, orderId={}", riderId, orderId);

        Flux<String> notificationStream = webTestClient.get()
                .uri("/riders/" + riderId + "/notifications/subscribe")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .returnResult(String.class)
                .getResponseBody();

        StepVerifier.create(notificationStream)
                .assertNext(event -> {
                    log.info(">>> [PHASE 1] Received Connection Event: {}", event);
                    assertThat(event).contains("riderId=" + riderId);
                })
                .then(() -> {
                    log.info(">>> [ACTION] Triggering Dispatch Request");
                    webTestClient.post()
                            .uri("/deliveries/" + orderId + "/dispatch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(new DispatchRequest(DeliveryType.SINGLE))
                            .exchange()
                            .expectStatus().isOk()
                            .expectBody()
                            .consumeWith(result -> {
                                String body = result.getResponseBodyContent() != null ? new String(result.getResponseBodyContent()) : "empty";
                                log.info(">>> [ACTION] Dispatch API Result: {}", body);
                            });
                })
                .assertNext(event -> {
                    log.info(">>> [PHASE 2] Received Dispatch Notification: {}", event);
                    assertThat(event).contains("\"orderId\":" + orderId);
                    assertThat(event).contains("\"riderId\":" + riderId);
                })
                .thenCancel()
                .verify(Duration.ofSeconds(15));
    }
}

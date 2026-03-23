package personal.yejin.foodDelivery.domain.order.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import personal.yejin.foodDelivery.domain.delivery.model.Delivery;
import personal.yejin.foodDelivery.domain.order.model.Order;
import personal.yejin.foodDelivery.domain.rider.model.Rider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderDeliveryNotificationServiceTest {

    @Test
    @DisplayName("성능 측정: 다수의 SSE Emitter로 알림 전송 소요 시간 테스트")
    void performance_measureAsyncSseBroadcast() throws Exception {
        // given
        OrderDeliveryNotificationService service = new OrderDeliveryNotificationService();
        Long orderId = 1000L;
        int clientCount = 5000; // 가상의 클라이언트 5천 명이 동시에 같은 주문의 알림을 기다린다고 가정

        // 5000개의 SseEmitter 구독 생성 (고의적인 지연을 유발하는 Mock Emitter 사용)
        for (int i = 0; i < clientCount; i++) {
            SseEmitter dummyEmitter = new SseEmitter() {
                @Override
                public void send(SseEventBuilder builder) throws IOException {
                    try {
                        // 네트워크 지연을 시뮬레이션 (1개의 메시지 전송당 5ms가 걸린다고 가정)
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            };
            
            // service의 private 로직 우회 접근 대신, 일반 Emitter를 넘겨도 테스트 가능하도록 
            // 구독 처리를 흉내냅니다 (현재 코드는 내부 orderEmitters 에 직접 넣을 수 없으니
            // 실제 구현체의 subscribeOrderNotification 메서드를 활용해 내부 map에 쌓이게 합니다. 
            // 하지만 내부에서 생성하는 Emitter를 제어할 수 없기 때문에 리플렉션으로 직접 넣음.
            addMockEmitterToService(service, orderId, dummyEmitter);
        }

        Delivery delivery = mock(Delivery.class);
        Order order = mock(Order.class);
        Rider rider = mock(Rider.class);
        
        when(delivery.getId()).thenReturn(1L);
        when(delivery.getOrder()).thenReturn(order);
        when(delivery.getRider()).thenReturn(rider);
        when(order.getId()).thenReturn(orderId);
        when(rider.getId()).thenReturn(500L);

        // when : 실제 발송 수행 및 시간 측정
        System.out.println("========== [성능 측정 시작] ==========");
        long startTime = System.currentTimeMillis();

        service.notifyNearArrival(delivery, 0.5);

        long endTime = System.currentTimeMillis();
        long durationMs = endTime - startTime;
        
        System.out.println("발송 대상 수: " + clientCount + " 명");
        System.out.println("메인 스레드 응답까지 걸린 시간: " + durationMs + " ms");
        System.out.println("=========================================");
        
        // 동기(for루프 블로킹) 방식이었다면: 5000 * 5ms = 약 25,000ms (25초)가 걸림
        // 현재 비동기(runAsync) 방식이므로: 메인 스레드는 작업을 스레드풀에 위임하고 즉시 종료됨 (100ms 이내 예상)

        // 메인 스레드가 즉각 반환되었는지 검증 (1초 내외로 끝나야 함)
        assertTrue(durationMs < 1000,
            "메인 스레드가 블로킹되어 지연이 발생했습니다. (실제 결과: " + durationMs + "ms)");
            
        // 비동기 작업들이 끝날 때까지 여유있게 조금 대기
        Thread.sleep(2000); 
    }

    @Test
    @DisplayName("성능 측정: (비교용) 기존 순차 동기 방식 시뮬레이션")
    void performance_measureSyncSseBroadcast() throws Exception {
        // given
        Long orderId = 1000L;
        int clientCount = 5000;
        
        List<SseEmitter> testEmitters = new ArrayList<>();
        // 5000개의 SseEmitter 구독 생성 (고의적인 지연을 유발하는 Mock Emitter 사용)
        for (int i = 0; i < clientCount; i++) {
            SseEmitter dummyEmitter = new SseEmitter() {
                @Override
                public void send(SseEventBuilder builder) throws IOException {
                    try {
                        // 네트워크 지연 시뮬레이션 (1개 전송당 5ms)
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            };
            testEmitters.add(dummyEmitter);
        }

        // when : 개선 전 기존 Service 코드와 동일한 형태의 동기 for 루프 실행
        System.out.println("========== [동기 방식 성능 측정 시작] ==========");
        long startTime = System.currentTimeMillis();

        for (SseEmitter emitter : testEmitters) {
            try {
                // 개선 전 코드처럼 CompletableFuture 없이 바로 현재 스레드에서 send 호출
                emitter.send(SseEmitter.event()
                        .name("delivery-near-arrival")
                        .data("payload"));
            } catch (IOException e) {
                // 예외 처리
            }
        }

        long endTime = System.currentTimeMillis();
        long durationMs = endTime - startTime;
        
        System.out.println("발송 대상 수: " + clientCount + " 명");
        System.out.println("기존 (동기 루프) 메인 스레드 대기 시간: " + durationMs + " ms");
        System.out.println("=========================================");

        // 사실상 5,000 * 5ms = 25,000ms 넘게 걸림을 확인
        assertTrue(durationMs >= 25000,
            "동기 전송 시 예상되는 지연 시간이 발생하지 않았습니다. (실제: " + durationMs + "ms)");
    }
    
    
    // 리플렉션을 통해 테스트용 가짜(Mock) Emitter 삽입
    @SuppressWarnings("unchecked")
    private void addMockEmitterToService(OrderDeliveryNotificationService service, Long orderId, SseEmitter emitter) throws Exception {
        java.lang.reflect.Field field = OrderDeliveryNotificationService.class.getDeclaredField("orderEmitters");
        field.setAccessible(true);
        java.util.Map<Long, List<SseEmitter>> orderEmitters = (java.util.Map<Long, List<SseEmitter>>) field.get(service);
        
        orderEmitters.computeIfAbsent(orderId, k -> new java.util.concurrent.CopyOnWriteArrayList<>()).add(emitter);
    }
}

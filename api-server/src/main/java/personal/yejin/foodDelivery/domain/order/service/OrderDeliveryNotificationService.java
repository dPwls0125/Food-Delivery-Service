package personal.yejin.foodDelivery.domain.order.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import personal.yejin.foodDelivery.domain.delivery.model.Delivery;
import personal.yejin.foodDelivery.domain.order.dto.OrderNearArrivalNotification;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class OrderDeliveryNotificationService {

    private static final long SSE_TIMEOUT_MILLIS = 60L * 60 * 1000;
    private static final String NEAR_ARRIVAL_EVENT_NAME = "delivery-near-arrival";

    private final Map<Long, List<SseEmitter>> orderEmitters = new ConcurrentHashMap<>();
    
    // 알림 전용 고정 스레드 풀 생성 (예: size 10)
    private final ExecutorService notificationExecutor = Executors.newFixedThreadPool(10);

    public SseEmitter subscribeOrderNotification(Long orderId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        orderEmitters.computeIfAbsent(orderId, key -> new CopyOnWriteArrayList<>()).add(emitter);

        registerEmitterLifecycleCallbacks(orderId, emitter);
        sendConnectEvent(orderId, emitter);
        return emitter;
    }

    public void notifyNearArrival(Delivery delivery, double remainingDistanceKm) {
        Long orderId = delivery.getOrder().getId();
        OrderNearArrivalNotification payload = OrderNearArrivalNotification.of(
                orderId,
                delivery.getId(),
                delivery.getRider().getId(),
                remainingDistanceKm
        );

        List<SseEmitter> emitters = orderEmitters.getOrDefault(orderId, List.of());
        for (SseEmitter emitter : emitters) {
            CompletableFuture.runAsync(() -> {
                try {
                    emitter.send(SseEmitter.event()
                            .name(NEAR_ARRIVAL_EVENT_NAME)
                            .data(payload));
                } catch (IOException e) {
                    log.warn("Near arrival SSE 전송 실패. orderId={}, deliveryId={}", orderId, delivery.getId(), e);
                    removeEmitter(orderId, emitter);
                }
            }, notificationExecutor);
        }
    }

    private void registerEmitterLifecycleCallbacks(Long orderId, SseEmitter emitter) {
        emitter.onCompletion(() -> removeEmitter(orderId, emitter));
        emitter.onTimeout(() -> removeEmitter(orderId, emitter));
        emitter.onError(error -> removeEmitter(orderId, emitter));
    }

    private void removeEmitter(Long orderId, SseEmitter emitter) {
        List<SseEmitter> emitters = orderEmitters.get(orderId);
        if (emitters == null) {
            return;
        }
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            orderEmitters.remove(orderId);
        }
    }

    private void sendConnectEvent(Long orderId, SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("orderId=" + orderId)
            );
        } catch (IOException e) {
            removeEmitter(orderId, emitter);
            throw new RuntimeException("Order SSE 연결 실패");
        }
    }
}

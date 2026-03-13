package personal.yejin.foodDelivery.domain.rider.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import personal.yejin.foodDelivery.domain.delivery.model.Delivery;
import personal.yejin.foodDelivery.domain.rider.dto.RiderDispatchNotification;

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
public class RiderDispatchNotificationService {
    private static final long SSE_TIMEOUT_MILLIS = 60L * 60 * 1000;
    private static final String DISPATCH_EVENT_NAME = "dispatch-assigned";
    private final Map<Long, List<SseEmitter>> riderEmitters = new ConcurrentHashMap<>(); // TODO : to server Stateless
    
    // 라이더 알림 전용 커스텀 스레드 풀 추가
    private final ExecutorService dispatchExecutor = Executors.newFixedThreadPool(10);

    public SseEmitter subscribeRiderNotification(Long riderId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        riderEmitters.computeIfAbsent(riderId, key -> new CopyOnWriteArrayList<>()).add(emitter);
        registerEmitterLifecycleCallbacks(riderId, emitter);
        sendConnectEvent(riderId, emitter);
        return emitter;
    }

    public void notifyDispatchAssigned(Delivery delivery) {

        if (delivery.getRider() == null) {
            log.warn("Rider 배차 스킵 deliveryId={}", delivery.getId());
            throw new IllegalArgumentException("Rider가 배차되지 않은 배달입니다.");
        }

        Long riderId = delivery.getRider().getId();
        RiderDispatchNotification payload = RiderDispatchNotification.of(riderId, delivery.getId(), delivery.getOrder().getId(), delivery.getDeliveryType());

        List<SseEmitter> emitters = riderEmitters.getOrDefault(riderId, List.of());
        for (SseEmitter emitter : emitters) {
            CompletableFuture.runAsync(() -> {
                try {
                    emitter.send(SseEmitter.event()
                            .name(DISPATCH_EVENT_NAME)
                            .data(payload));
                } catch (IOException e) {
                    log.warn("SSE 전송 실패. riderId={}, deliveryId={}", riderId, delivery.getId(), e);
                    removeEmitter(riderId, emitter);
                }
            }, dispatchExecutor);
        }
    }

    private void registerEmitterLifecycleCallbacks(Long riderId, SseEmitter emitter) {
        emitter.onCompletion(() -> removeEmitter(riderId, emitter));
        emitter.onTimeout(() -> removeEmitter(riderId, emitter));
        emitter.onError(error -> removeEmitter(riderId, emitter));
    }

    private void removeEmitter(Long riderId, SseEmitter emitter) {
        List<SseEmitter> emitters = riderEmitters.get(riderId);
        if (emitters == null) {
            return;
        }
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            riderEmitters.remove(riderId);
        }
    }

    private void sendConnectEvent(Long riderId, SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("riderId=" + riderId)
            );
        } catch (IOException e) {
            removeEmitter(riderId, emitter);
            throw new RuntimeException("SSE 연결 실패");
        }
    }
}

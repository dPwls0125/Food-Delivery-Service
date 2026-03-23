package personal.yejin.foodDelivery.domain.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import personal.yejin.foodDelivery.domain.payment.event.PaymentCompletedEvent;
import personal.yejin.foodDelivery.domain.payment.event.PaymentFailedEvent;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class NotificationService {

    // 실서버에서는 Redis Pub/Sub을 활용하여 분산 환경 SSE를 구현해야 하나, 우선 로컬 Map으로 구성합니다.
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 30; // 30분 동안 SSE 연결 유지

    /**
     * 클라이언트가 SSE 구독을 요청할 때 Emitter를 생성하여 반환합니다.
     */
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> {
            log.info("SSE emitter 완료됨. userId={}", userId);
            emitters.remove(userId);
        });
        emitter.onTimeout(() -> {
            log.info("SSE emitter 타임아웃됨. userId={}", userId);
            emitter.complete();
            emitters.remove(userId);
        });
        emitter.onError((e) -> {
            log.error("SSE emitter 에러. userId={}", userId, e);
            emitter.completeWithError(e);
            emitters.remove(userId);
        });

        // 503 에러 방지를 위한 더미 이벤트 발송
        sendToClient(userId, "INIT", "Event stream created. [userId=" + userId + "]");
        log.info("SSE 연결 생성 완료. userId={}", userId);

        return emitter;
    }

    private void sendToClient(Long userId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
                log.info("SSE 푸시 발송 성공 - userId: {}, eventName: {}", userId, eventName);
            } catch (IOException exception) {
                emitters.remove(userId);
                log.error("SSE 푸시 발송 실패 - 연결을 삭제합니다. userId: {}", userId, exception);
            }
        } else {
            // 접속 중이지 않은 사용자 (이 경우 푸시 알림 FCM 서버 등으로 우회 전송하는 로직이 필요)
            log.debug("SSE 연결이 존재하지 않습니다. userId: {}", userId);
        }
    }

    /**
     * 결제 성공(PaymentCompleted) 이벤트를 수신하고 사용자에게 푸시합니다.
     */
    @KafkaListener(topics = "payment-events", groupId = "notification-group")
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        log.info("[Notification] 결제 성공 이벤트 수신! orderId={}, userId={}", event.orderId(), event.userId());
        
        // 클라이언트에게 결제 완료 신호 발송
        sendToClient(event.userId(), "PAYMENT_SUCCESS", "결제가 정상적으로 완료되었습니다. 주문번호: " + event.orderId());
        
        // 추가로: 가맹점 사장님(Store Owner) 단말기에도 "새로운 주문이 들어왔습니다!" 이벤트를 발생시켜야 합니다.
        // 현재는 userId 기준이므로 추후 storeId -> ownerId 매핑을 찾아 발송하는 로직이 덧붙여집니다.
    }

    /**
     * 결제 실패(PaymentFailed) 이벤트를 수신하고 사용자에게 푸시합니다.
     */
    @KafkaListener(topics = "payment-events", groupId = "notification-group")
    public void handlePaymentFailedEvent(PaymentFailedEvent event) {
        log.warn("[Notification] 결제 실패 이벤트 수신! orderId={}, userId={}, 사유={}", event.orderId(), event.userId(), event.reason());

        // 클라이언트에게 결제 실패 신호 발송
        sendToClient(event.userId(), "PAYMENT_FAILED", "결제가 실패했습니다. 사유: " + event.reason());
    }
}

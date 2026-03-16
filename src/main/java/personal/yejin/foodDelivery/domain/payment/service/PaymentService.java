package personal.yejin.foodDelivery.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal.yejin.foodDelivery.domain.order.event.OrderCreatedEvent;
import personal.yejin.foodDelivery.domain.order.model.Order;
import personal.yejin.foodDelivery.domain.order.repository.OrderRepository;
import personal.yejin.foodDelivery.domain.payment.event.PaymentCompletedEvent;
import personal.yejin.foodDelivery.domain.payment.event.PaymentFailedEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentClient paymentClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 비동기 결제 파이프라인 (100% EDA)
     * OrderService가 발행한 OrderCreatedEvent를 구독하여 결제를 진행합니다.
     */
    @KafkaListener(topics = "order-events", groupId = "payment-group")
    @Transactional
    public void processEventBasedPayment(OrderCreatedEvent event) {
        log.info("결제 요청 이벤트 수신: orderId={}, amount={}", event.orderId(), event.amount());

        try {
            // DB에 주문이 실제로 생성되었는지 (PENDING/CREATED) 확인
            Order order = orderRepository.findById(event.orderId())
                    .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + event.orderId()));

            // 1. 외부 결제 API 호출
            boolean paymentSuccess = paymentClient.pay(event.amount(), event.paymentMethod());

            if (paymentSuccess) {
                // 결제 성공 이벤트 발행 -> (OrderService 상태변경, NotificationService SSE 알림)
                log.info("결제 승인 완료. PaymentCompletedEvent 발행: orderId={}", event.orderId());
                kafkaTemplate.send("payment-events", new PaymentCompletedEvent(
                        event.orderId(),
                        event.userId(),
                        "tx-" + event.orderId()
                ));
            } else {
                // 잔액 부족, 한도 초과 등
                log.warn("결제 승인 실패. PaymentFailedEvent 발행: orderId={}", event.orderId());
                kafkaTemplate.send("payment-events", new PaymentFailedEvent(
                        event.orderId(),
                        event.userId(),
                        "결제 한도 초과/잔액 부족"
                ));
            }
        } catch (Exception e) {
            // 시스템 에러 등으로 인한 실패 방어 로직
            log.error("결제 시스템 처리 중 에러 발생: orderId={}, message={}", event.orderId(), e.getMessage());
            kafkaTemplate.send("payment-events", new PaymentFailedEvent(
                    event.orderId(),
                    event.userId(),
                    "시스템 오류: " + e.getMessage()
            ));
        }
    }
}

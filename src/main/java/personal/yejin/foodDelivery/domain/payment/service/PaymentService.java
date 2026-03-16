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
import personal.yejin.foodDelivery.domain.payment.model.Payment;
import personal.yejin.foodDelivery.domain.payment.model.PaymentStatus;
import personal.yejin.foodDelivery.domain.payment.repository.PaymentRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentClient paymentClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PaymentRepository paymentRepository;

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

            // [추가] 결제 시도 전 이력을 영속화 (PENDING)
            Payment payment = Payment.builder()
                    .orderId(event.orderId())
                    .userId(event.userId())
                    .amount(event.amount())
                    .paymentMethod(event.paymentMethod())
                    .status(PaymentStatus.PENDING)
                    .build();
            paymentRepository.save(payment);

            // 1. 외부 결제 API 호출
            boolean paymentSuccess = paymentClient.pay(event.amount(), event.paymentMethod());

            if (paymentSuccess) {
                // 결제 성공 시 엔티티 상태 업데이트 (SUCCESS) 및 거래 번호 세팅
                String paymentKey = "tx-" + UUID.randomUUID().toString(); // 실무에서는 PG사 응답 Key 매핑
                payment.complete(paymentKey);
                // JPA 더티 체킹으로 트랜잭션 종료 시 update 쿼리 발생

                // 결제 완료 (Event-Carried State Transfer 기반 풍부한 페이로드 세팅)
                log.info("결제 승인 완료. PaymentCompletedEvent 발행: orderId={}", event.orderId());
                kafkaTemplate.send("payment-events", new PaymentCompletedEvent(
                        event.orderId(),
                        event.userId(),
                        UUID.randomUUID().toString(), // correlationId
                        event.amount(),
                        event.paymentMethod(),
                        paymentKey
                ));
            } else {
                // 잔액 부족, 한도 초과 등 실패 시 엔티티 상태 업데이트 (FAILED)
                String failReason = "결제 한도 초과/잔액 부족";
                payment.fail(failReason);

                log.warn("결제 승인 실패. PaymentFailedEvent 발행: orderId={}", event.orderId());
                kafkaTemplate.send("payment-events", new PaymentFailedEvent(
                        event.orderId(),
                        event.userId(),
                        failReason,
                        event.amount(),
                        event.paymentMethod()
                ));
            }
        } catch (Exception e) {
            // 시스템 에러 발생 시 처리
            log.error("결제 시스템 처리 중 에러 발생: orderId={}, message={}", event.orderId(), e.getMessage());
            
            // 결제(Payment) 객체를 만들기도 전에 예외가 터졌을 가능성과, 만든 후 터진 가능성이 혼재하므로
            // (이 경우 보통 트랜잭션 롤백이 되며, 외부 모듈 실패 등을 로깅/이벤트로 전파만 함)
            kafkaTemplate.send("payment-events", new PaymentFailedEvent(
                    event.orderId(),
                    event.userId(),
                    "시스템 오류: " + e.getMessage(),
                    event.amount(),
                    event.paymentMethod()
            ));
        }
    }
}

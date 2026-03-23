package personal.yejin.foodDelivery.domain.payment.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import personal.yejin.foodDelivery.domain.order.dto.OrderPaymentRequest;
import personal.yejin.foodDelivery.domain.order.dto.OrderPaymentResponse;
import personal.yejin.foodDelivery.domain.payment.event.PaymentCompletedEvent;
import personal.yejin.foodDelivery.domain.payment.event.PaymentFailedEvent;
import personal.yejin.foodDelivery.domain.payment.model.Payment;
import personal.yejin.foodDelivery.domain.payment.model.PaymentStatus;
import personal.yejin.foodDelivery.domain.payment.repository.PaymentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PaymentService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PaymentRepository paymentRepository;
    private final Map<OrderPaymentRequest.PaymentMethod, PaymentAPI> paymentProcessors;

    public PaymentService(KafkaTemplate<String, Object> kafkaTemplate, PaymentRepository paymentRepository, List<PaymentAPI> paymentAPIs) {
        this.kafkaTemplate = kafkaTemplate;
        this.paymentRepository = paymentRepository;
        this.paymentProcessors = paymentAPIs.stream()
                .collect(Collectors.toMap(
                        PaymentAPI::getSupportedMethod,
                        Function.identity()));
    }

    @Transactional
    public OrderPaymentResponse processPayment(long orderId, String correlationId, OrderPaymentRequest event) {

        log.info("결제 요청 이벤트 수신: orderId={}, correlationId={}", orderId, correlationId);

        Payment payment = Payment.builder()
                .orderId(orderId)
                .userId(event.userId())
                .amount(event.finalPrice())
                .paymentMethod(event.paymentMethod().name())
                .status(PaymentStatus.PENDING)
                .build();

        try {
            paymentRepository.save(payment);

            PaymentAPI paymentAPI = paymentProcessors.get(event.paymentMethod());
            boolean paymentSuccess = paymentAPI.pay(event.finalPrice(), event.paymentMethod());

            if (paymentSuccess) {
                String paymentKey = "tx-" + UUID.randomUUID(); // 실무에서는 PG사 응답 Key 매핑
                payment.complete(paymentKey);
                log.info("결제 승인 완료.: orderId={}", orderId);

                TransactionSynchronizationManager.registerSynchronization(
                        new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                kafkaTemplate.send("payment-events", new PaymentCompletedEvent(
                                        orderId,
                                        event.userId(),
                                        correlationId,
                                        event.finalPrice(),
                                        payment.getPaymentMethod(),
                                        paymentKey
                                ));
                            }
                        }
                );

                return OrderPaymentResponse.builder()
                        .orderId(orderId)
                        .paymentStatus(PaymentStatus.SUCCESS)
                        .paidAt(LocalDateTime.now())
                        .build();

            } else {
                String failReason = "결제 한도 초과/잔액 부족";
                payment.fail(failReason);
                log.warn("결제 승인 실패. PaymentFailedEvent 발행: orderId={}", orderId);
                TransactionSynchronizationManager.registerSynchronization(
                        new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                kafkaTemplate.send("payment-events", new PaymentFailedEvent(
                                        orderId,
                                        event.userId(),
                                        correlationId,
                                        failReason,
                                        event.finalPrice(),
                                        payment.getPaymentMethod()
                                ));
                            }
                        }
                );

                return OrderPaymentResponse.builder()
                        .orderId(orderId)
                        .paymentStatus(PaymentStatus.FAILED)
                        .build();
            }

        } catch (Exception e) {
            log.error("결제 시스템 처리 중 에러 발생, rollback: orderId={}, correlationId={}, message={}", orderId, correlationId, e.getMessage());
            kafkaTemplate.send("payment-events", new PaymentFailedEvent(
                    orderId,
                    event.userId(),
                    correlationId,
                    e.getMessage(),
                    event.finalPrice(),
                    payment.getPaymentMethod()
            ));
            throw e;
        }
    }
}


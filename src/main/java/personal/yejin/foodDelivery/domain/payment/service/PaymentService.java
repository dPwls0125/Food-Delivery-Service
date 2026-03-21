package personal.yejin.foodDelivery.domain.payment.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
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

        try {
            Payment payment = Payment.builder()
                    .orderId(orderId)
                    .userId(event.userId())
                    .amount(event.finalPrice())
                    .paymentMethod(event.paymentMethod().name())
                    .status(PaymentStatus.PENDING)
                    .build();

            paymentRepository.save(payment); // 결제 시도 중에 오류 날 상황을 대비하여 미리 payment정보를 미리 올려 둠.

            PaymentAPI paymentAPI = paymentProcessors.get(event.paymentMethod());
            boolean paymentSuccess = paymentAPI.pay(event.finalPrice(), event.paymentMethod());

            if (paymentSuccess) {
                String paymentKey = "tx-" + UUID.randomUUID(); // 실무에서는 PG사 응답 Key 매핑
                payment.complete(paymentKey);
                log.info("결제 승인 완료.: orderId={}", orderId);

                kafkaTemplate.send("payment-events", new PaymentCompletedEvent(
                        orderId,
                        event.userId(),
                        correlationId,
                        event.finalPrice(),
                        payment.getPaymentMethod(),
                        paymentKey
                ));

                return OrderPaymentResponse.builder()
                        .orderId(orderId)
                        .paymentStatus(OrderPaymentResponse.PaymentStatus.SUCCESS)
                        .paidAt(LocalDateTime.now())
                        .build();

            } else {
                String failReason = "결제 한도 초과/잔액 부족";
                payment.fail(failReason);

                log.warn("결제 승인 실패. PaymentFailedEvent 발행: orderId={}", orderId);
                kafkaTemplate.send("payment-events", new PaymentFailedEvent(
                        orderId,
                        event.userId(),
                        correlationId,
                        failReason,
                        event.finalPrice(),
                        payment.getPaymentMethod()
                ));

                return OrderPaymentResponse.builder()
                        .orderId(orderId)
                        .paymentStatus(OrderPaymentResponse.PaymentStatus.FAILED)
                        .build();
            }

        } catch (Exception e) {
            log.error("결제 시스템 처리 중 에러 발생: orderId={}, message={}", orderId, e.getMessage());
            kafkaTemplate.send("payment-events", new PaymentFailedEvent(
                    orderId,
                    event.userId(),
                    correlationId,
                    "시스템 오류: " + e.getMessage(),
                    event.finalPrice(),
                    event.paymentMethod().name()
            ));

            return OrderPaymentResponse.builder()
                    .orderId(orderId)
                    .paymentStatus(OrderPaymentResponse.PaymentStatus.FAILED)
                    .build();
        }
    }
}


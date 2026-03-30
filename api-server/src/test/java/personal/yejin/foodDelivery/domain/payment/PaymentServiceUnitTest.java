package personal.yejin.foodDelivery.domain.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import personal.yejin.foodDelivery.service.PaymentService;
import personal.yejin.foodDelivery.dto.PaymentResponse;
import personal.yejin.foodDelivery.exception.PaymentException;
import personal.yejin.model.PaymentMethod;
import personal.yejin.model.PaymentStatus;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceUnitTest {

    @Mock
    KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    PaymentService paymentService;

    @Test
    @DisplayName("payment-request 이벤트 발행 성공시, PaymentResponse를 반환한다.")
    void Payment_Request_Success() {
        // given
        CompletableFuture future = CompletableFuture.completedFuture(mock(SendResult.class));
        when(kafkaTemplate.send(anyString(), any())).thenReturn(future);

        // when
        PaymentResponse response = paymentService.processPayment(1L, 1L, 10000, PaymentMethod.CARD);
        // then
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(response.getCorrelationId()).startsWith("payment-");
    }

    @Test
    @DisplayName("KafkaTimeout 발생시, PaymentException이 발생한다.")
    void Kafka_전송_타임아웃시_PaymentException_발생() {
        // given - 영원히 완료되지 않는 future
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), any())).thenReturn(future);

        // when & then
        assertThatThrownBy(() -> paymentService.processPayment(
                1L, 1L, 10000, PaymentMethod.CARD))
                .isInstanceOf(PaymentException.class);
    }

    @Test
    @DisplayName("Payment-request 이벤트 발행 실패시, PaymentException을 던진다.")
    void Kafka_전송실패시_PaymentException_발생() {
        // given
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("broker down"));
        when(kafkaTemplate.send(anyString(), any())).thenReturn(future);

        // when & then
        assertThatThrownBy(() -> paymentService.processPayment(
                1L, 1L, 10000, PaymentMethod.CARD))
                .isInstanceOf(PaymentException.class);
    }

}

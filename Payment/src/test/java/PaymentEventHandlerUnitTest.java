import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import personal.yejin.PaymentResultEvent;
import personal.yejin.client.StatusServerClient;
import personal.yejin.handler.PaymentCompletedInternalEvent;
import personal.yejin.handler.PaymentEventHandler;
import personal.yejin.model.PaymentStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEventHandlerUnitTest {

    @Mock
    KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    StatusServerClient statusServerClient;

    @InjectMocks
    PaymentEventHandler handler;

    @Test
    @DisplayName("결제 완료 이벤트를 받으면 Kafka 결과 이벤트를 발행하고, Status Server에 상태를 업데이트한다.")
    void 성공_이벤트_처리시_Kafka_발행_및_StatusServer_업데이트() {
        PaymentCompletedInternalEvent event = new PaymentCompletedInternalEvent(
                "corr-1", 100L, 5001L, 1L, 101L,
                PaymentStatus.SUCCESS, LocalDateTime.now(), 18000, null
        );

        handler.handlePaymentCompleted(event);

        // Kafka 발행 검증
        ArgumentCaptor<PaymentResultEvent> captor = ArgumentCaptor.forClass(PaymentResultEvent.class);
        verify(kafkaTemplate).send(eq("payment-result"), captor.capture());

        PaymentResultEvent result = captor.getValue();
        assertThat(result.paymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(result.orderId()).isEqualTo(5001L);
        assertThat(result.failureReason()).isNull();

        // Status Server 업데이트 검증
        verify(statusServerClient).updatePaymentStatus(
                eq(5001L), eq("corr-1"), eq(PaymentStatus.SUCCESS), isNull());
    }

    @Test
    @DisplayName("실패 이벤트 처리시 실패 사유가 포함된 Kafka 이벤트를 발행한다.")
    void 실패_이벤트_처리시_실패사유_포함() {
        PaymentCompletedInternalEvent event = new PaymentCompletedInternalEvent(
                "corr-2", 101L, 5002L, 2L, 102L,
                PaymentStatus.FAIL, LocalDateTime.now(), 10000, "잔액 부족"
        );

        handler.handlePaymentCompleted(event);

        ArgumentCaptor<PaymentResultEvent> captor = ArgumentCaptor.forClass(PaymentResultEvent.class);
        verify(kafkaTemplate).send(eq("payment-result"), captor.capture());

        PaymentResultEvent result = captor.getValue();
        assertThat(result.paymentStatus()).isEqualTo(PaymentStatus.FAIL);
        assertThat(result.failureReason()).isEqualTo("잔액 부족");

        verify(statusServerClient).updatePaymentStatus(
                eq(5002L), eq("corr-2"), eq(PaymentStatus.FAIL), eq("잔액 부족"));
    }

    @Test
    @DisplayName("Status Server 업데이트 실패해도 예외가 전파되지 않는다.")
    void StatusServer_실패해도_예외_전파_안됨() {
        PaymentCompletedInternalEvent event = new PaymentCompletedInternalEvent(
                "corr-3", 102L, 5003L, 3L, 103L,
                PaymentStatus.SUCCESS, LocalDateTime.now(), 15000, null
        );

        doThrow(new RuntimeException("연결 실패"))
                .when(statusServerClient).updatePaymentStatus(anyLong(), anyString(), any(), any());

        // 예외가 전파되지 않아야 함
        handler.handlePaymentCompleted(event);

        // Kafka는 정상 발행됨
        verify(kafkaTemplate).send(eq("payment-result"), any(PaymentResultEvent.class));
    }
}

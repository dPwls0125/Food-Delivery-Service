import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import personal.yejin.PaymentRequestEvent;
import personal.yejin.PaymentResultEvent;
import personal.yejin.kafkaListner.PaymentRequestListener;
import personal.yejin.model.Payment;
import personal.yejin.model.PaymentMethod;
import personal.yejin.model.PaymentStatus;
import personal.yejin.repository.PaymentRepository;
import personal.yejin.service.PaymentAPI;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentRequestListenerTest {

    @Mock
    Map<PaymentMethod, PaymentAPI> paymentAPIMap;

    @Mock
    KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    PaymentRepository paymentRepository; // TODO : 가짜객체이므로 JPA가 동작하지 않는다. 따라서 통합테스트로 변경하여 실제로 상태가 SUCCESS로 바뀌어서 DB에 반영됐는지"를 검증

    @InjectMocks
    PaymentRequestListener listener;

    @Mock
    PaymentAPI paymentAPI;

    PaymentRequestEvent request = new PaymentRequestEvent(
            1L, 1L, "user-1", 10000, PaymentMethod.CARD
    );

    @BeforeEach
    void setUp() {
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentAPIMap.get(any(PaymentMethod.class))).thenReturn(paymentAPI);
    }


    // TODO : 후에 status update에 대한 로직도 추가해야함.
    @Test
    @DisplayName("결제 성공시, payment를 DB에 SUCCESS로 저장하고, 성공 이벤트를 발행한다.")
    void 결제_성공시_SUCCESS로_저장되고_이벤트_발행() {
        when(paymentAPI.pay(10000, PaymentMethod.CARD)).thenReturn(true);

        listener.consumePaymentRequest(request);

        verify(paymentRepository, times(1)).save(argThat(
                payment -> payment.getStatus() == PaymentStatus.SUCCESS
        ));

        verify(kafkaTemplate).send(eq("payment-result"), argThat(
                event -> ((PaymentResultEvent) event).failureReason() == null
        ));
    }

    @Test
    @DisplayName("결제 API 처리 실패시, payment를 DB에 FAILED로 저장하고, 실패 이벤트를 발행한다.")
    void 결제_실패시_FAILED로_저장되고_실패사유_담김() {
        when(paymentAPI.pay(10000, PaymentMethod.CARD)).thenReturn(false);

        listener.consumePaymentRequest(request);

        verify(paymentRepository, times(1)).save(argThat(
                payment -> payment.getStatus() == PaymentStatus.FAILED
        ));
        verify(kafkaTemplate).send(eq("payment-result"), argThat(
                event -> ((PaymentResultEvent) event).failureReason() != null
        ));
    }

    @Test
    @DisplayName("결제 API 처리중 예외 발생시, payment를 DB에 FAILED로 저장하고, 실패 이벤트를 발행한다.")
    void 예외_발생해도_FAILED_이벤트_발행() {
        when(paymentAPI.pay(10000, PaymentMethod.CARD))
                .thenThrow(new RuntimeException("PG사 타임아웃"));

        listener.consumePaymentRequest(request);

        verify(kafkaTemplate).send(eq("payment-result"), argThat(
                event -> ((PaymentResultEvent) event).failureReason()
                        .contains("PG사 타임아웃")
        ));
    }
}

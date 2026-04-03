import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import personal.yejin.PaymentRequestEvent;
import personal.yejin.handler.PaymentCompletedInternalEvent;
import personal.yejin.kafkaListner.PaymentRequestListener;
import personal.yejin.model.Payment;
import personal.yejin.model.PaymentMethod;
import personal.yejin.model.PaymentStatus;
import personal.yejin.repository.PaymentRepository;
import personal.yejin.service.PaymentAPI;

import java.util.List;
import java.util.Map;
 
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentRequestListenerTest {

    @Mock
    PaymentRepository paymentRepository;
 
    @Mock
    ApplicationEventPublisher eventPublisher;
 
    PaymentRequestListener listener;
 
    @Mock
    PaymentAPI paymentAPI;

    PaymentRequestEvent request = new PaymentRequestEvent(
            1L, 1L, "user-1", 10000, PaymentMethod.CARD
    );

    @BeforeEach
    void setUp() {
        when(paymentAPI.getSupportedMethod()).thenReturn(PaymentMethod.CARD);
        listener = new PaymentRequestListener(paymentRepository, eventPublisher, List.of(paymentAPI));

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("결제 성공시, Payment를 PENDING으로 저장하고, SUCCESS 상태의 내부 이벤트를 발행한다.")
    void 결제_성공시_SUCCESS_이벤트_발행() {
        when(paymentAPI.pay(10000, PaymentMethod.CARD)).thenReturn(true);

        listener.consumePaymentRequest(request);

        // Payment가 PENDING 상태로 save 호출됨 (dirty checking으로 이후 SUCCESS로 변경)
        verify(paymentRepository, times(1)).save(any(Payment.class));

        // Spring 내부 이벤트가 SUCCESS 상태로 발행됨
        ArgumentCaptor<PaymentCompletedInternalEvent> captor =
                ArgumentCaptor.forClass(PaymentCompletedInternalEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        PaymentCompletedInternalEvent event = captor.getValue();
        assertThat(event.paymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(event.failureReason()).isNull();
        assertThat(event.orderId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("결제 API 처리 실패시, FAIL 상태의 내부 이벤트를 발행한다.")
    void 결제_실패시_FAIL_이벤트_발행() {
        when(paymentAPI.pay(10000, PaymentMethod.CARD)).thenReturn(false);

        listener.consumePaymentRequest(request);

        ArgumentCaptor<PaymentCompletedInternalEvent> captor =
                ArgumentCaptor.forClass(PaymentCompletedInternalEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        PaymentCompletedInternalEvent event = captor.getValue();
        assertThat(event.paymentStatus()).isEqualTo(PaymentStatus.FAIL);
        assertThat(event.failureReason()).isNotNull();
    }

    @Test
    @DisplayName("결제 API 처리중 예외 발생시, FAIL 상태의 내부 이벤트를 발행한다.")
    void 예외_발생해도_FAIL_이벤트_발행() {
        when(paymentAPI.pay(10000, PaymentMethod.CARD))
                .thenThrow(new RuntimeException("PG사 타임아웃"));

        listener.consumePaymentRequest(request);

        ArgumentCaptor<PaymentCompletedInternalEvent> captor =
                ArgumentCaptor.forClass(PaymentCompletedInternalEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        PaymentCompletedInternalEvent event = captor.getValue();
        assertThat(event.paymentStatus()).isEqualTo(PaymentStatus.FAIL);
        assertThat(event.failureReason()).contains("PG사 타임아웃");
    }
}

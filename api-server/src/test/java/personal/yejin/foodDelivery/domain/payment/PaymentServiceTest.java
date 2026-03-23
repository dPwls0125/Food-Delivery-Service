package personal.yejin.foodDelivery.domain.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import personal.yejin.foodDelivery.domain.order.dto.OrderPaymentRequest;
import personal.yejin.foodDelivery.domain.order.dto.OrderPaymentResponse;
import personal.yejin.foodDelivery.domain.payment.event.PaymentFailedEvent;
import personal.yejin.foodDelivery.domain.payment.model.Payment;
import personal.yejin.foodDelivery.domain.payment.model.PaymentStatus;
import personal.yejin.foodDelivery.domain.payment.repository.PaymentRepository;
import personal.yejin.foodDelivery.domain.payment.service.PaymentAPI;
import personal.yejin.foodDelivery.domain.payment.service.PaymentService;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/*
검증 하고 싶은 시나리오.
scenario 1. 결제가 성공한 경우, payment 정보를 success로 저장하고, commit된 이후에 성공 이벤트가 발행된다.
scenario 2. 결제가 실패한 경우, payment 정보를 fail로 저장하고, commit된 이후에 실패 이벤트가 발행된다.
 */

@SpringBootTest
public class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockitoBean
    private PaymentAPI paymentAPI;

    @MockitoBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @BeforeEach
    void setUp(){
        paymentRepository.deleteAll();
    }

    @Test
    @DisplayName("결제가 성공한 경우, payment 정보를 success로 저장하고, 메서드가 종료되어 commit된 이후에 성공 이벤트가 발행된다.")
    void whenPaymentSuccess_succecssEventIsPublished_afterCommit(){
        // given
        AtomicBoolean committedDataVisibleWhenEventPublished = new AtomicBoolean(false);

        given(paymentAPI.pay(any(),any())).willReturn(Boolean.FALSE);
        given(kafkaTemplate.send(eq("payment-events"),any()))
                .willAnswer( invocation -> {
                    List<Payment> payments = paymentRepository.findAll();
                    assertThat(payments).hasSize(1);
                    assertThat(payments.getFirst().getPaymentMethod()).isEqualTo(PaymentStatus.SUCCESS);
                    committedDataVisibleWhenEventPublished.set(true);
                    return null;
                });

        OrderPaymentRequest request = new OrderPaymentRequest(OrderPaymentRequest.PaymentMethod.CASH, 1000, 1L);
        //when

        OrderPaymentResponse paymentResponse = paymentService.processPayment(1L,"kakao111", request);

        // then
        assertThat(paymentResponse.paymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        List<Payment> payments = paymentRepository.findAll();
        assertThat(payments).hasSize(1);
        assertThat(payments.getFirst().getStatus()).isEqualTo(PaymentStatus.SUCCESS);

        verify(kafkaTemplate, times(1)).send(eq("payment-events"), any());
        assertThat(committedDataVisibleWhenEventPublished.get()).isTrue();

        // 전달된 인자 포획기
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(eq("payment-events"), captor.capture());
        assertThat(captor.getValue()).isInstanceOf(PaymentFailedEvent.class);
    }



    @Test
    @DisplayName("결제가 실패한 경우, payment 정보를 fail로 저장하고, commit된 이후에 실패 이벤트가 발행된다.")
    void whenPaymentFail_failEventIsPublished_afterCommit(){

    }


    @Test
    @DisplayName("예외가 발생한 경우, 1. 에러를 로깅하고, 2. 실패 메세지를 발행하고, 3. 예외를 다시 던져 rollback이 가능하도록 한다.") // TODO: 이게 정녕 맞는지 고민.
    void whenAPIThrowException_logError_publishFailEvent(){

    }



}

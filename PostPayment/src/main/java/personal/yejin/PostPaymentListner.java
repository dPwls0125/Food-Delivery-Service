package personal.yejin;


import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PostPaymentListner
{

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // 결제 후처리.
    @KafkaListener(topics = "payment-result", groupId = "payment-result-group")
    public void consumePaymentResult(PaymentRequestEvent event) {



    }
}

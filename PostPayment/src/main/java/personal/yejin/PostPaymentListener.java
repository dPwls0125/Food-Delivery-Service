package personal.yejin;


import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PostPaymentListener
{

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // 결제 후
    @KafkaListener(topics = "payment-result", groupId = "notify-to-store")
    public void consumePaymentResultAndNotifyToStore(PaymentResultEvent event) {



    }
}

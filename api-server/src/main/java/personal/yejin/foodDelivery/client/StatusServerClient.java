package personal.yejin.foodDelivery.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import personal.yejin.foodDelivery.dto.PaymentStatusResponse;

@Slf4j
@Component
public class StatusServerClient {

    private final RestClient restClient;

    public StatusServerClient(@Value("${status-server.url:http://localhost:8082}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * status-server에서 결제 상태를 조회한다.
     * 클라이언트 폴링 시 사용된다.
     */
    public PaymentStatusResponse getPaymentStatus(Long orderId) {
        try {
            return restClient.get()
                    .uri("/status/payment/{orderId}", orderId)
                    .retrieve()
                    .body(PaymentStatusResponse.class);
        } catch (Exception e) {
            log.warn("status-server 조회 실패: orderId={}", orderId, e);
            return null;
        }
    }
}

package personal.yejin.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import personal.yejin.model.PaymentStatus;

import java.time.Duration;

@Slf4j
@Component
public class StatusServerClient {

    private final RestClient restClient;

    public StatusServerClient(@Value("${status-server.url:http://localhost:8082}") String baseUrl) {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * 결제 처리 완료 후 status-server에 상태를 전송한다.
     * status-server는 이 정보를 Redis에 TTL 5분으로 캐싱한다.
     */
    public void updatePaymentStatus(long orderId, String correlationId, PaymentStatus status, String failureReason) {
        try {
            restClient.put()
                    .uri("/status/payment/{orderId}", orderId)
                    .body(new PaymentStatusUpdateRequest(correlationId, status, failureReason))
                    .retrieve()
                    .toBodilessEntity();

            log.info("status-server 상태 업데이트 성공: orderId={}, correlationId={}, status={}",
                    orderId, correlationId, status);
        } catch (Exception e) {
            // status-server 실패가 결제 흐름을 중단시키면 안 됨
            log.error("status-server 상태 업데이트 실패 (무시): orderId={}, correlationId={}",
                    orderId, correlationId, e);
        }
    }

    private record PaymentStatusUpdateRequest(
            String correlationId,
            PaymentStatus status,
            String failureReason
    ) {
    }
}

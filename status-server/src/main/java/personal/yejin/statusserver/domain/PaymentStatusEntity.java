package personal.yejin.statusserver.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import personal.yejin.model.PaymentStatus;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "payment:status")
public class PaymentStatusEntity {

    @Id
    private Long orderId; // payment:status:{orderId}

    private PaymentStatus status;
    private String correlationId;
    private String failureReason;
    private String updatedAt;

    @TimeToLive
    private Long expiration;
}

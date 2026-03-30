package personal.yejin.statusserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import personal.yejin.statusserver.domain.PaymentStatusEntity;
import personal.yejin.statusserver.dto.PaymentStatusResponse;
import personal.yejin.statusserver.dto.PaymentStatusUpdateRequest;
import personal.yejin.statusserver.repository.PaymentStatusRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentStatusService {

    // 초 단위로 5분 TTL
    private static final Long TTL_SECONDS = 300L;

    private final PaymentStatusRepository paymentStatusRepository;

    public void updateStatus(Long orderId, PaymentStatusUpdateRequest request) {

        PaymentStatusEntity entity = PaymentStatusEntity.builder()
                .orderId(orderId)
                .status(request.status())
                .correlationId(request.correlationId())
                .failureReason(request.failureReason() != null ? request.failureReason() : "")
                .updatedAt(LocalDateTime.now().toString())
                .expiration(TTL_SECONDS)
                .build();

        paymentStatusRepository.save(entity);
    }

    public PaymentStatusResponse getStatus(Long orderId) {
        return paymentStatusRepository.findById(orderId)
                .map(entity -> new PaymentStatusResponse(
                        entity.getOrderId(),
                        entity.getStatus(),
                        entity.getCorrelationId(),
                        emptyToNull(entity.getFailureReason()),
                        entity.getUpdatedAt()
                ))
                .orElse(null);
    }

    private String emptyToNull(String value) {
        return (value == null || value.isEmpty()) ? null : value;
    }
}


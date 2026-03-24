package personal.yejin.foodDelivery.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import personal.yejin.model.PaymentStatus;

@AllArgsConstructor
@Getter
@Setter
public class PaymentResponse {
    String correlationId;
    PaymentStatus status;
}

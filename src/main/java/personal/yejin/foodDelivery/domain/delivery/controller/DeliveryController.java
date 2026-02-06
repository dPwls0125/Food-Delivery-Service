package personal.yejin.foodDelivery.domain.delivery.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import personal.yejin.foodDelivery.domain.delivery.dto.DeliveryStatusUpdateRequest;
import personal.yejin.foodDelivery.domain.delivery.dto.DeliveryStatusUpdateResponse;
import personal.yejin.foodDelivery.domain.delivery.dto.DispatchRequest;
import personal.yejin.foodDelivery.domain.delivery.dto.DispatchResponse;
import personal.yejin.foodDelivery.domain.delivery.model.DispatchStatus;

@RestController
@RequestMapping("/deliveries")
public class DeliveryController {
    @PostMapping("/{orderId}/dispatch")
    public ResponseEntity<DispatchResponse> requestDispatch(
            @PathVariable Long orderId,
            @RequestBody DispatchRequest request
    ) {
        DispatchResponse fakeResponse = new DispatchResponse(
                orderId,
                DispatchStatus.REQUESTED,
                request.deliveryType()
        );
        return ResponseEntity.ok(fakeResponse);
    }

    @PostMapping("/{deliveryId}/status")
    public ResponseEntity<DeliveryStatusUpdateResponse> updateDeliveryStatus(
            @PathVariable Long deliveryId,
            @RequestBody DeliveryStatusUpdateRequest request
    ) {
        DeliveryStatusUpdateResponse fakeResponse = new DeliveryStatusUpdateResponse(
                deliveryId,
                request.status()
        );
        return ResponseEntity.ok(fakeResponse);
    }
}

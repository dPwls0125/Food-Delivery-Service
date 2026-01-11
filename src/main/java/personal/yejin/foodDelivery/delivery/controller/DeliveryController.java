package personal.yejin.foodDelivery.delivery.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import personal.yejin.foodDelivery.delivery.dto.DeliveryStatusUpdateRequest;
import personal.yejin.foodDelivery.delivery.dto.DeliveryStatusUpdateResponse;
import personal.yejin.foodDelivery.delivery.dto.DispatchRequest;
import personal.yejin.foodDelivery.delivery.dto.DispatchResponse;
import personal.yejin.foodDelivery.delivery.model.DispatchStatus;

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

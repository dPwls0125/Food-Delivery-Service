package personal.yejin.statusserver.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import personal.yejin.statusserver.dto.PaymentStatusResponse;
import personal.yejin.statusserver.dto.PaymentStatusUpdateRequest;
import personal.yejin.statusserver.service.PaymentStatusService;

@RestController
@RequestMapping("/status/payment")
@RequiredArgsConstructor
public class PaymentStatusController {

    private final PaymentStatusService paymentStatusService;

    @PutMapping("/{orderId}")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody PaymentStatusUpdateRequest request) {
        paymentStatusService.updateStatus(orderId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentStatusResponse> getStatus(@PathVariable Long orderId) {
        PaymentStatusResponse response = paymentStatusService.getStatus(orderId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }
}

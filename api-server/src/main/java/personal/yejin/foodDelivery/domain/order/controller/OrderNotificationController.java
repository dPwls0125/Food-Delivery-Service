package personal.yejin.foodDelivery.domain.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import personal.yejin.foodDelivery.domain.order.service.OrderDeliveryNotificationService;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderNotificationController {

    private final OrderDeliveryNotificationService orderDeliveryNotificationService;

    @GetMapping(path = "/{orderId}/notifications/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable Long orderId) {
        return orderDeliveryNotificationService.subscribeOrderNotification(orderId);
    }
}

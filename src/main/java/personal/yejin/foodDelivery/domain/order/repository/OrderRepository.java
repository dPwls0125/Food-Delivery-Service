package personal.yejin.foodDelivery.domain.order.repository;

import personal.yejin.foodDelivery.domain.order.model.Order;

import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long id);
}

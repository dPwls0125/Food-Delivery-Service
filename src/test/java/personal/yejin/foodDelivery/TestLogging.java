package personal.yejin.foodDelivery;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import personal.yejin.foodDelivery.domain.delivery.model.DeliveryType;
import personal.yejin.foodDelivery.domain.delivery.service.DeliveryFacade;
import personal.yejin.foodDelivery.domain.order.model.Order;
import personal.yejin.foodDelivery.domain.order.repository.OrderRepository;

@SpringBootTest
@Transactional
class DeliveryFacadeTxTest {

	@Autowired
	DeliveryFacade deliveryFacade;

	@Autowired
	OrderRepository orderRepository;

	@Test
	void transactionLoggingTest() {
		orderRepository.save(Order.builder().build());
		deliveryFacade.dispatchRiderAndCreateDelivery(1L, DeliveryType.BUNDLE);
	}
}

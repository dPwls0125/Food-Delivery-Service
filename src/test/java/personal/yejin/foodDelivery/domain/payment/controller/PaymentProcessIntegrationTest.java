package personal.yejin.foodDelivery.domain.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryType;
import personal.yejin.foodDelivery.domain.order.dto.OrderPaymentRequest;
import personal.yejin.foodDelivery.domain.order.model.Order;
import personal.yejin.foodDelivery.domain.order.model.OrderItem;
import personal.yejin.foodDelivery.domain.order.model.OrderStatus;
import personal.yejin.foodDelivery.domain.order.repository.OrderRepository;
import personal.yejin.foodDelivery.domain.rider.model.Location;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PaymentProcessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        OrderItem item1 = OrderItem.builder().menuId(1L).name("김밥").unitPrice(4000).quantity(2).build();
        OrderItem item2 = OrderItem.builder().menuId(2L).name("떡볶이").unitPrice(5000).quantity(1).build();

        testOrder = Order.builder()
                .storeId(101L)
                .userId(1L)
                .pickupLocation(new Location(37.5, 127.0))
                .deliveryLocation(new Location(37.51, 127.01))
                .deliveryAddress("서울시 강남구")
                .deliveryType(DeliveryType.SINGLE) // Delivery fee 5000
                .orderStatus(OrderStatus.CREATED)
                .customerNote("문앞에 두고 가주세요")
                .orderItems(List.of(item1, item2))
                .build();
        
        item1.setOrder(testOrder);
        item2.setOrder(testOrder);

        orderRepository.save(testOrder);
    }

    @Test
    @DisplayName("기본 결제 처리 성공 시 상태가 PAID로 변경되고 응답이 내려온다")
    void processPaymentSuccess() throws Exception {
        // given
        OrderPaymentRequest request = new OrderPaymentRequest(
                OrderPaymentRequest.PaymentMethod.CARD,
                10L, // 쿠폰 아이디
                true // 배민 클럽 사용 여부
        );
        
        // foodPrice = 8000 + 5000 = 13000
        // deliveryFee = 5000
        // originalPrice = 18000
        // couponDiscount = 3000
        // baeminClubDiscount = 5000 * 0.1 = 500
        // totalDiscount = 3500
        // paidAmount = 18000 - 3500 = 14500

        // when
        ResultActions result = mockMvc.perform(post("/payment/{orderId}", testOrder.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then: API 응답 확인
        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.orderId").value(testOrder.getId()))
              .andExpect(jsonPath("$.orderStatus").value("PAID"))
              .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"))
              .andExpect(jsonPath("$.originalPrice").value(18000))
              .andExpect(jsonPath("$.discountAmount").value(3500))
              .andExpect(jsonPath("$.paidAmount").value(14500))
              .andExpect(jsonPath("$.paymentMethod").value("CARD"));

        // then: DB 상태 확인
        Order updatedOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
        assertThat(updatedOrder.getOrderStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(updatedOrder.getFinalPrice()).isEqualTo(14500);
    }
}

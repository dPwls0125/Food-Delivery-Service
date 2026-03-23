package personal.yejin.foodDelivery.domain.order;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import personal.yejin.foodDelivery.domain.order.controller.OrderController;
import personal.yejin.foodDelivery.domain.order.dto.OrderCreateRequest;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("주문 생성 API 테스트")
    void testCreateOrder() throws Exception {
        // given
        OrderCreateRequest.OrderItemRequest orderItem = new OrderCreateRequest.OrderItemRequest(1L, 2);
        OrderCreateRequest request = new OrderCreateRequest(101L, Collections.singletonList(orderItem), "서울시 강남구 테헤란로 123");

        // when & then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.orderId").isNumber())
                .andExpect(jsonPath("$.orderStatus").exists())
                .andExpect(jsonPath("$.orderStatus").isString())
                .andExpect(jsonPath("$.totalPrice").exists())
                .andExpect(jsonPath("$.totalPrice").isNumber());
    }

    @Test
    @DisplayName("주문 상세 조회 API 테스트")
    void testGetOrder() throws Exception {
        // given
        long orderId = 5001L;

        // when & then
        mockMvc.perform(get("/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.orderId").isNumber())
                .andExpect(jsonPath("$.store.name").exists())
                .andExpect(jsonPath("$.store.name").isString())
                .andExpect(jsonPath("$.store.address").exists())
                .andExpect(jsonPath("$.store.address").isString())
                .andExpect(jsonPath("$.deliveryAddress").exists())
                .andExpect(jsonPath("$.deliveryAddress").isString())
                .andExpect(jsonPath("$.orderStatus").exists())
                .andExpect(jsonPath("$.orderStatus").isString())
                .andExpect(jsonPath("$.items").exists())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0]").exists())
                .andExpect(jsonPath("$.items[0].name").exists())
                .andExpect(jsonPath("$.items[0].name").isString())
                .andExpect(jsonPath("$.items[0].quantity").exists())
                .andExpect(jsonPath("$.items[0].quantity").isNumber())
                .andExpect(jsonPath("$.customerNote").exists())
                .andExpect(jsonPath("$.customerNote").isString());
    }
}

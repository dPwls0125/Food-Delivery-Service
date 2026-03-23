package personal.yejin.foodDelivery.domain.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import personal.yejin.foodDelivery.domain.order.dto.DiscountPreviewRequest;
import personal.yejin.foodDelivery.domain.order.dto.OrderPaymentRequest;
import personal.yejin.foodDelivery.domain.payment.controller.PaymentController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("할인 적용 결과 반환 API 테스트")
    void testPreviewDiscounts() throws Exception {
        // given
        long orderId = 5001L;
        DiscountPreviewRequest request = new DiscountPreviewRequest(10L, true);

        // when & then
        mockMvc.perform(post("/payment/" + orderId + "/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.orderId").isNumber())
                .andExpect(jsonPath("$.originalPrice").exists())
                .andExpect(jsonPath("$.originalPrice").isNumber())
                .andExpect(jsonPath("$.discountDetails.couponDiscount").exists())
                .andExpect(jsonPath("$.discountDetails.couponDiscount").isNumber())
                .andExpect(jsonPath("$.discountDetails.baeminClubDiscount").exists())
                .andExpect(jsonPath("$.discountDetails.baeminClubDiscount").isNumber())
                .andExpect(jsonPath("$.totalDiscountAmount").exists())
                .andExpect(jsonPath("$.totalDiscountAmount").isNumber())
                .andExpect(jsonPath("$.finalPrice").exists())
                .andExpect(jsonPath("$.finalPrice").isNumber());
    }

    @Test
    @DisplayName("주문 결제 API 테스트")
    void testProcessPayment() throws Exception {
        // given
        long orderId = 5001L;
        OrderPaymentRequest request = new OrderPaymentRequest(OrderPaymentRequest.PaymentMethod.CARD, 10_000, 1);

        // when & then
        mockMvc.perform(post("/payment/" + orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.orderId").isNumber())
                .andExpect(jsonPath("$.orderStatus").exists())
                .andExpect(jsonPath("$.orderStatus").isString())
                .andExpect(jsonPath("$.paymentStatus").exists())
                .andExpect(jsonPath("$.paymentStatus").isString())
                .andExpect(jsonPath("$.originalPrice").exists())
                .andExpect(jsonPath("$.originalPrice").isNumber())
                .andExpect(jsonPath("$.discountAmount").exists())
                .andExpect(jsonPath("$.discountAmount").isNumber())
                .andExpect(jsonPath("$.paidAmount").exists())
                .andExpect(jsonPath("$.paidAmount").isNumber())
                .andExpect(jsonPath("$.paymentMethod").exists())
                .andExpect(jsonPath("$.paymentMethod").isString())
                .andExpect(jsonPath("$.paidAt").exists())
                .andExpect(jsonPath("$.paidAt").isString());
    }
}

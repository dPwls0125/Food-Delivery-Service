package personal.yejin.foodDelivery.order.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("주문 생성에 성공하면 201 Created 상태와 주문 정보를 반환한다.")
    void createOrderSuccess() throws Exception {
        // Given
        String requestJson = """
            {
              "storeId": 101,
              "orderItems": [
                {
                  "menuId": 1,
                  "quantity": 2
                },
                {
                  "menuId": 3,
                  "quantity": 1
                }
              ],
              "deliveryAddress": "서울시 강남구 테헤란로 123"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderId").value(5001))
            .andExpect(jsonPath("$.orderStatus").value("CREATED"))
            .andExpect(jsonPath("$.totalPrice").value(18000))
            .andDo(print());
    }

    @Test
    @DisplayName("라이더의 주문 상세 조회에 성공하면 200 OK 상태와 주문 상세 정보를 반환한다.")
    void getRiderOrderDetailSuccess() throws Exception {
        // Given
        long orderId = 5001L;

        // When & Then
        mockMvc.perform(get("/api/orders/" + orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(orderId))
            .andExpect(jsonPath("$.store.name").value("김밥천국"))
            .andExpect(jsonPath("$.items[0].name").value("김밥"))
            .andExpect(jsonPath("$.items[0].quantity").value(2))
            .andExpect(jsonPath("$.customerNote").value("문 앞에 놔주세요"))
            .andDo(print());
    }

    @Test
    @DisplayName("주문 할인 미리보기에 성공하면 200 OK 상태와 할인 적용 결과를 반환한다.")
    void previewDiscountsSuccess() throws Exception {
        // Given
        long orderId = 5001L;
        String requestJson = """
            {
              "couponId": 10,
              "useBaeminClub": true
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/order/" + orderId + "/discounts/preview")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(orderId))
            .andExpect(jsonPath("$.originalPrice").value(18000))
            .andExpect(jsonPath("$.discountDetails.couponDiscount").value(3000))
            .andExpect(jsonPath("$.discountDetails.baeminClubDiscount").value(1500))
            .andExpect(jsonPath("$.totalDiscountAmount").value(4500))
            .andExpect(jsonPath("$.finalPrice").value(13500))
            .andDo(print());
    }

    @Test
    @DisplayName("주문 결제에 성공하면 200 OK 상태와 결제 완료 정보를 반환한다.")
    void processPaymentSuccess() throws Exception {
        // Given
        long orderId = 5001L;
        String requestJson = """
            {
              "paymentMethod": "CARD",
              "couponId": 10,
              "useBaeminClub": true
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/order/" + orderId + "/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(orderId))
            .andExpect(jsonPath("$.orderStatus").value("PAID"))
            .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"))
            .andExpect(jsonPath("$.originalPrice").value(18000))
            .andExpect(jsonPath("$.discountAmount").value(4500))
            .andExpect(jsonPath("$.paidAmount").value(13500))
            .andExpect(jsonPath("$.paymentMethod").value("CARD"))
            .andExpect(jsonPath("$.paidAt").value("2026-01-05T15:10:00"))
            .andDo(print());
    }

    @Test
    @DisplayName("라이더 매칭 요청에 성공하면 200 OK 상태와 매칭 상태를 반환한다.")
    void requestDispatchSuccess() throws Exception {
        // Given
        long orderId = 5001L;
        String requestJson = """
            {
              "deliveryType": "BUNDLE"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/order/" + orderId + "/dispatch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(orderId))
            .andExpect(jsonPath("$.dispatchStatus").value("REQUESTED"))
            .andExpect(jsonPath("$.deliveryType").value("BUNDLE"))
            .andDo(print());
    }

    @Test
    @DisplayName("배달 상태 변경에 성공하면 200 OK 상태와 변경된 상태를 반환한다.")
    void updateDeliveryStatusSuccess() throws Exception {
        // Given
        long orderId = 5001L;
        String requestJson = """
            {
              "status": "PICKED_UP"
            }
            """;
        // When & Then
        mockMvc.perform(post("/api/orders/" + orderId + "/delivery-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(orderId))
            .andExpect(jsonPath("$.updatedStatus").value("PICKED_UP"))
            .andDo(print());
    }

    @Test
    @DisplayName("사용자의 라이더 위치 조회에 성공하면 200 OK 상태와 위치 정보를 반환한다.")
    void getRiderLocationSuccess() throws Exception {
        // Given
        long orderId = 5001L;

        // When & Then
        mockMvc.perform(get("/api/orders/" + orderId + "/rider-location"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(orderId))
            .andExpect(jsonPath("$.latitude").value(37.498095))
            .andExpect(jsonPath("$.longitude").value(127.027610))
            .andExpect(jsonPath("$.lastUpdatedAt").value("2026-01-05T16:15:00"))
            .andDo(print());
    }
}

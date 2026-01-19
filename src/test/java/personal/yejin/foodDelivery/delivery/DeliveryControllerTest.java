package personal.yejin.foodDelivery.delivery;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import personal.yejin.foodDelivery.domain.delivery.controller.DeliveryController;
import personal.yejin.foodDelivery.domain.delivery.dto.DeliveryStatusUpdateRequest;
import personal.yejin.foodDelivery.domain.delivery.dto.DispatchRequest;
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryStatus;
import personal.yejin.foodDelivery.domain.delivery.model.DeliveryType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeliveryController.class)
public class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("라이더 매칭 요청 API 테스트")
    void testRequestDispatch() throws Exception {
        // given
        long orderId = 5001L;
        DispatchRequest request = new DispatchRequest(DeliveryType.BUNDLE);

        // when & then
        mockMvc.perform(post("/deliveries/" + orderId + "/dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.orderId").isNumber())
                .andExpect(jsonPath("$.dispatchStatus").exists())
                .andExpect(jsonPath("$.dispatchStatus").isString())
                .andExpect(jsonPath("$.deliveryType").exists())
                .andExpect(jsonPath("$.deliveryType").isString());
    }

    @Test
    @DisplayName("배달 상태 변경 API 테스트")
    void testUpdateDeliveryStatus() throws Exception {
        // given
        long deliveryId = 1L;
        DeliveryStatusUpdateRequest request = new DeliveryStatusUpdateRequest(DeliveryStatus.PICKED_UP);

        // when & then
        mockMvc.perform(post("/deliveries/" + deliveryId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.orderId").isNumber())
                .andExpect(jsonPath("$.updatedStatus").exists())
                .andExpect(jsonPath("$.updatedStatus").isString());
    }
}

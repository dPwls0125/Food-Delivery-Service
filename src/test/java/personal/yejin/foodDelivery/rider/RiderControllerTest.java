package personal.yejin.foodDelivery.rider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import personal.yejin.foodDelivery.rider.controller.RiderController;
import personal.yejin.foodDelivery.rider.dto.RiderLocationRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RiderController.class)
public class RiderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("라이더 위치 조회 API 테스트")
    void testGetRiderLocation() throws Exception {
        // given
        long riderId = 5001L;

        // when & then
        mockMvc.perform(get("/riders/" + riderId + "/location"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.riderId").exists())
                .andExpect(jsonPath("$.riderId").isNumber())
                .andExpect(jsonPath("$.latitude").exists())
                .andExpect(jsonPath("$.latitude").isNumber())
                .andExpect(jsonPath("$.longitude").exists())
                .andExpect(jsonPath("$.longitude").isNumber())
                .andExpect(jsonPath("$.lastUpdatedAt").exists())
                .andExpect(jsonPath("$.lastUpdatedAt").isString());
    }

    @Test
    @DisplayName("라이더 위치 업데이트 API 테스트")
    void testUpdateRiderLocation() throws Exception {
        // given
        long riderId = 5001L;
        RiderLocationRequest request = new RiderLocationRequest(37.498095, 127.027610);

        // when & then
        mockMvc.perform(post("/riders/" + riderId + "/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.riderId").exists())
                .andExpect(jsonPath("$.riderId").isNumber())
                .andExpect(jsonPath("$.latitude").exists())
                .andExpect(jsonPath("$.latitude").isNumber())
                .andExpect(jsonPath("$.longitude").exists())
                .andExpect(jsonPath("$.longitude").isNumber())
                .andExpect(jsonPath("$.lastUpdatedAt").exists())
                .andExpect(jsonPath("$.lastUpdatedAt").isString());
    }
}

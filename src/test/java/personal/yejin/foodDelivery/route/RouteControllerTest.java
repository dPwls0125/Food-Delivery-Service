package personal.yejin.foodDelivery.route;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import personal.yejin.foodDelivery.route.controller.RouteController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RouteController.class)
public class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("라이더 현재 배달 Route 조회 API 테스트")
    void testGetCurrentRoute() throws Exception {
        // when & then
        mockMvc.perform(get("/routes/me/current-route"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stops").exists())
                .andExpect(jsonPath("$.stops").isArray())
                .andExpect(jsonPath("$.stops[0]").exists())
                .andExpect(jsonPath("$.stops[0].sequence").exists())
                .andExpect(jsonPath("$.stops[0].sequence").isNumber())
                .andExpect(jsonPath("$.stops[0].type").exists())
                .andExpect(jsonPath("$.stops[0].type").isString())
                .andExpect(jsonPath("$.stops[0].orderId").exists())
                .andExpect(jsonPath("$.stops[0].orderId").isNumber())
                .andExpect(jsonPath("$.stops[0].address").exists())
                .andExpect(jsonPath("$.stops[0].address").isString());
    }
}

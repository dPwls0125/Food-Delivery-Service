package personal.yejin.foodDelivery.rider.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RiderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("라이더의 현재 배달 경로 조회에 성공하면 200 OK 상태와 경로 정보를 반환한다.")
    void getCurrentRouteSuccess() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/riders/me/current-route"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stops").isArray())
            .andExpect(jsonPath("$.stops.length()").value(4))
            .andExpect(jsonPath("$.stops[0].sequence").value(1))
            .andExpect(jsonPath("$.stops[0].type").value("PICKUP"))
            .andExpect(jsonPath("$.stops[0].orderId").value(5001))
            .andExpect(jsonPath("$.stops[0].address").value("가게 A"))
            .andExpect(jsonPath("$.stops[2].sequence").value(3))
            .andExpect(jsonPath("$.stops[2].type").value("DELIVERY"))
            .andDo(print());
    }
}

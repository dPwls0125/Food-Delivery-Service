package personal.yejin.foodDelivery.route.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("라이더의 위치 전송에 성공하면 202 Accepted 상태를 반환한다.")
    void updateLocationSuccess() throws Exception {
        // Given
        long routeId = 123L;
        String requestJson = """
            {
              "latitude": 37.498095,
              "longitude": 127.027610
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/routes/" + routeId + "/location")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isAccepted())
            .andDo(print());
    }
}

package personal.yejin.foodDelivery.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import personal.yejin.foodDelivery.domain.auth.controller.AuthController;
import personal.yejin.foodDelivery.domain.auth.dto.LoginRequest;
import personal.yejin.foodDelivery.domain.auth.dto.SignUpRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원 가입 API 테스트")
    void testSignUp() throws Exception {
        // given
        SignUpRequest request = new SignUpRequest(
                "user@example.com",
                "Password123!",
                "김예진",
                SignUpRequest.SignUpType.USER
        );

        // when & then
        mockMvc.perform(post("/auth/sign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.email").isString())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.name").isString())
                .andExpect(jsonPath("$.nickname").exists())
                .andExpect(jsonPath("$.nickname").isString())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.createdAt").isString());
    }

    @Test
    @DisplayName("로그인 API 테스트")
    void testLogin() throws Exception {
        // given
        LoginRequest request = new LoginRequest("user@example.com", "Password123!");

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.userType").exists())
                .andExpect(jsonPath("$.userType").isString())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.name").isString());
    }
}

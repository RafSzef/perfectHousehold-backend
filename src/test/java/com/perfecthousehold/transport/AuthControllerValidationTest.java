package com.perfecthousehold.transport;

import com.perfecthousehold.config.JacksonConfig;
import com.perfecthousehold.security.service.AuthService;
import com.perfecthousehold.security.service.UserService;
import com.perfecthousehold.transport.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        AuthController.class,
        GlobalExceptionHandler.class,
        JacksonConfig.class
})
class AuthControllerValidationTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestBootApp {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @Test
    void registerRejectsBlankFieldsWith400ValidationProblemDetail() throws Exception {
        String invalidPayload = """
                {
                    "username": " ",
                    "email": "not-an-email",
                    "passwordHash": ""
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.type").value("https://perfecthousehold.local/errors/validation-error"))
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.passwordHash").exists())
                .andExpect(jsonPath("$.properties").doesNotExist());

        verifyNoInteractions(authService);
    }

    @Test
    void loginRejectsBlankCredentialsWith400ValidationProblemDetail() throws Exception {
        String invalidPayload = """
                {
                    "username": "",
                    "password": "  "
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.password").exists());

        verifyNoInteractions(authService);
    }

    @Test
    void changePasswordRejectsBlankFieldsWith400ValidationProblemDetail() throws Exception {
        String invalidPayload = """
                {
                    "currentPassword": "",
                    "newPassword": ""
                }
                """;

        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.currentPassword").exists())
                .andExpect(jsonPath("$.errors.newPassword").exists());

        verifyNoInteractions(authService);
    }
}

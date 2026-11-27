package com.perfecthousehold.transport.exception;

import com.perfecthousehold.config.JacksonConfig;
import com.perfecthousehold.security.auth.UserPrincipal;
import com.perfecthousehold.security.config.SecurityConfig;
import com.perfecthousehold.security.entity.User;
import com.perfecthousehold.security.jwt.JwtAuthenticationFilter;
import com.perfecthousehold.security.jwt.JwtTokenProvider;
import com.perfecthousehold.security.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SecurityExceptionHandlingTest.TestSecuredController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtTokenProvider.class,
        GlobalExceptionHandler.class,
        JacksonConfig.class,
        SecurityExceptionHandlingTest.TestSecuredController.class
})
@TestPropertySource(properties = {
        "security.jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
        "security.jwt.expiration-ms=3600000"
})
class SecurityExceptionHandlingTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestBootApp {
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void unauthenticatedRequestToProtectedEndpointReturns401ProblemDetail() throws Exception {
        mockMvc.perform(get("/api/test-security/protected"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.type").value("https://perfecthousehold.local/errors/unauthorized"))
                .andExpect(jsonPath("$.instance").value("/api/test-security/protected"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.properties").doesNotExist());
    }

    @Test
    void authenticatedNonAdminAccessingAdminEndpointReturns403ProblemDetail() throws Exception {
        User user = new User();
        user.setId(10L);
        user.setUsername("regular_user");
        user.setEmail("user@example.com");
        user.setRole("ROLE_USER");
        user.setEnabled(true);
        UserPrincipal principal = UserPrincipal.create(user);

        when(customUserDetailsService.loadUserByUsername("regular_user")).thenReturn(principal);
        String token = jwtTokenProvider.generateToken(principal);

        mockMvc.perform(get("/api/test-security/admin")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.title").value("Forbidden"))
                .andExpect(jsonPath("$.type").value("https://perfecthousehold.local/errors/forbidden"))
                .andExpect(jsonPath("$.instance").value("/api/test-security/admin"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.properties").doesNotExist());
    }

    @RestController
    @RequestMapping("/api/test-security")
    static class TestSecuredController {

        @GetMapping("/protected")
        String protectedEndpoint() {
            return "ok";
        }

        @PreAuthorize("hasRole('ADMIN')")
        @GetMapping("/admin")
        String adminEndpoint() {
            return "admin-ok";
        }
    }
}

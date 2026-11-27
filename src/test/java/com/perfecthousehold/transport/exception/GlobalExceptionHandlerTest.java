package com.perfecthousehold.transport.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.perfecthousehold.config.JacksonConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new JacksonConfig().objectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void testProblemDetailSerializesExtensionsAtTopLevel() throws Exception {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid input");
        problem.setTitle("Bad Request");
        problem.setType(URI.create("https://perfecthousehold.local/errors/bad-request"));
        problem.setProperty("timestamp", Instant.parse("2026-01-01T12:00:00Z"));
        problem.setProperty("errors", Map.of("username", "must not be blank"));

        String json = objectMapper.writeValueAsString(problem);
        JsonNode root = objectMapper.readTree(json);

        assertFalse(root.has("properties"), "RFC 9457 extension fields must not be nested under 'properties'");
        assertEquals("2026-01-01T12:00:00Z", root.get("timestamp").asText());
        assertEquals("must not be blank", root.get("errors").get("username").asText());
    }

    @Test
    void testAuthenticationExceptionReturns401ProblemDetail() throws Exception {
        mockMvc.perform(get("/test-errors/auth"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.type").value("https://perfecthousehold.local/errors/unauthorized"))
                .andExpect(jsonPath("$.instance").value("/test-errors/auth"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.properties").doesNotExist());
    }

    @Test
    void testAccessDeniedExceptionReturns403ProblemDetail() throws Exception {
        mockMvc.perform(get("/test-errors/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.title").value("Forbidden"))
                .andExpect(jsonPath("$.type").value("https://perfecthousehold.local/errors/forbidden"))
                .andExpect(jsonPath("$.instance").value("/test-errors/forbidden"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testIllegalArgumentExceptionReturns400ProblemDetail() throws Exception {
        mockMvc.perform(get("/test-errors/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Current password does not match"))
                .andExpect(jsonPath("$.type").value("https://perfecthousehold.local/errors/bad-request"))
                .andExpect(jsonPath("$.instance").value("/test-errors/bad-request"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testDataIntegrityViolationReturns409WithoutSqlLeak() throws Exception {
        mockMvc.perform(get("/test-errors/conflict"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail").value("The request conflicts with existing data."))
                .andExpect(jsonPath("$.type").value("https://perfecthousehold.local/errors/conflict"))
                .andExpect(jsonPath("$.instance").value("/test-errors/conflict"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testMalformedJsonReturnsEnriched400ProblemDetail() throws Exception {
        mockMvc.perform(post("/test-errors/payload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").value("https://perfecthousehold.local/errors/bad-request"))
                .andExpect(jsonPath("$.instance").value("/test-errors/payload"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testUnhandledExceptionReturns500WithoutInternalDetails() throws Exception {
        mockMvc.perform(get("/test-errors/unhandled"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred. Please try again later."))
                .andExpect(jsonPath("$.type").value("https://perfecthousehold.local/errors/internal-error"))
                .andExpect(jsonPath("$.instance").value("/test-errors/unhandled"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @RestController
    @RequestMapping("/test-errors")
    static class TestController {

        @GetMapping("/auth")
        void auth() {
            throw new BadCredentialsException("Bad credentials");
        }

        @GetMapping("/forbidden")
        void forbidden() {
            throw new AccessDeniedException("Denied");
        }

        @GetMapping("/bad-request")
        void badRequest() {
            throw new IllegalArgumentException("Current password does not match");
        }

        @GetMapping("/conflict")
        void conflict() {
            throw new DataIntegrityViolationException("ERROR: duplicate key value violates unique constraint \"users_email_key\"");
        }

        @PostMapping("/payload")
        void payload(@RequestBody Map<String, Object> body) {
        }

        @GetMapping("/unhandled")
        void unhandled() {
            throw new RuntimeException("Secret DB connection string leaked");
        }
    }
}

package com.perfecthousehold.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.perfecthousehold.config.JacksonConfig;
import com.perfecthousehold.security.dao.UserDao;
import com.perfecthousehold.security.entity.User;
import com.perfecthousehold.security.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenericServiceTest {

    @Mock
    private UserDao userDao;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userDao);
        ObjectMapper objectMapper = new JacksonConfig().objectMapper();
        ReflectionTestUtils.setField(userService, "objectMapper", objectMapper);
    }

    @Test
    void testMapDbObjectFromJsonStringWithoutDto() {
        String jsonPayload = """
                {
                    "username": "jan_kowalski",
                    "email": "jan@example.com",
                    "passwordHash": "secret123",
                    "extraIgnoredField": "extraValue"
                }
                """;

        User user = userService.mapDbObject(jsonPayload, User.class);

        assertNotNull(user);
        assertEquals("jan_kowalski", user.getUsername());
        assertEquals("jan@example.com", user.getEmail());
        assertEquals("secret123", user.getPasswordHash());
    }

    @Test
    void testFindByUsernameWithRowToJsonString() {
        // Simulates PostgreSQL row_to_json(t) returned by MyBatis UserDao
        String dbRowToJson = """
                {"id":1,"username":"jan_kowalski","email":"jan@example.com","passwordHash":"secret123","role":"ROLE_USER","enabled":true}
                """;

        when(userDao.findByUsername("jan_kowalski")).thenReturn(dbRowToJson);

        User user = userService.findByUsername("jan_kowalski");

        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("jan_kowalski", user.getUsername());
        assertEquals("jan@example.com", user.getEmail());
        assertEquals("secret123", user.getPasswordHash());
        assertEquals("ROLE_USER", user.getRole());
        assertTrue(user.isEnabled());
    }

    @Test
    void testRegisterUserWithParamAnnotation() {
        User user = new User();
        user.setUsername("nowy_user");
        user.setEmail("nowy@example.com");
        user.setPasswordHash("tajne123");

        User registered = userService.registerUser(user);

        assertNotNull(registered);
        assertEquals("ROLE_USER", registered.getRole());
        assertTrue(registered.isEnabled());
        verify(userDao).insert(eq(user));
    }
}

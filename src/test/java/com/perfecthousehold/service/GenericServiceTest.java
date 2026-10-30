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
    void testFindByIdWithRowToJsonString() {
        String dbRowToJson = """
                {"id":42,"username":"admin","email":"admin@example.com","passwordHash":"$2a$10$hash","role":"ROLE_ADMIN","enabled":true}
                """;

        when(userDao.findById(42L)).thenReturn(dbRowToJson);

        User user = userService.findById(42L);

        assertNotNull(user);
        assertEquals(42L, user.getId());
        assertEquals("admin", user.getUsername());
        assertEquals("ROLE_ADMIN", user.getRole());
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

    @Test
    void testFindAllWithJsonAggString() {
        String dbJsonAgg = """
                [
                    {"id":1,"username":"admin","email":"admin@example.com","role":"ROLE_ADMIN","enabled":true},
                    {"id":2,"username":"jan","email":"jan@example.com","role":"ROLE_USER","enabled":true}
                ]
                """;

        when(userDao.findAll()).thenReturn(dbJsonAgg);

        var users = userService.findAll();

        assertNotNull(users);
        assertEquals(2, users.size());
        assertEquals("admin", users.get(0).getUsername());
        assertEquals("ROLE_ADMIN", users.get(0).getRole());
        assertEquals("jan", users.get(1).getUsername());
        assertEquals("ROLE_USER", users.get(1).getRole());
    }

    @Test
    void testMapDbListWithNullOrEmptyJsonAgg() {
        var nullResult = userService.mapDbList(null, User.class);
        assertNotNull(nullResult);
        assertTrue(nullResult.isEmpty());

        var emptyStringResult = userService.mapDbList("   ", User.class);
        assertNotNull(emptyStringResult);
        assertTrue(emptyStringResult.isEmpty());

        var literalNullStringResult = userService.mapDbList("null", User.class);
        assertNotNull(literalNullStringResult);
        assertTrue(literalNullStringResult.isEmpty());

        var emptyArrayResult = userService.mapDbList("[]", User.class);
        assertNotNull(emptyArrayResult);
        assertTrue(emptyArrayResult.isEmpty());
    }
}

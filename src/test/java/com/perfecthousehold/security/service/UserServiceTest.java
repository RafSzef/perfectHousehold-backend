package com.perfecthousehold.security.service;

import com.perfecthousehold.config.JacksonConfig;
import com.perfecthousehold.generic.mapper.DbObjectMapper;
import com.perfecthousehold.security.dao.UserDao;
import com.perfecthousehold.security.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    private UserService userService;

    @BeforeEach
    void setUp() {
        DbObjectMapper dbObjectMapper = new DbObjectMapper(new JacksonConfig().objectMapper());
        userService = new UserService(userDao, dbObjectMapper);
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
    void testRegisterUserWithDefaultRole() {
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

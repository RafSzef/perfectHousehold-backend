package com.perfecthousehold.generic.mapper;

import com.perfecthousehold.config.JacksonConfig;
import com.perfecthousehold.security.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DbObjectMapperTest {

    private DbObjectMapper dbObjectMapper;

    @BeforeEach
    void setUp() {
        dbObjectMapper = new DbObjectMapper(new JacksonConfig().objectMapper());
    }

    @Test
    void testMapFromJsonStringWithoutDto() {
        String jsonPayload = """
                {
                    "username": "jan_kowalski",
                    "email": "jan@example.com",
                    "passwordHash": "secret123",
                    "extraIgnoredField": "extraValue"
                }
                """;

        User user = dbObjectMapper.map(jsonPayload, User.class);

        assertNotNull(user);
        assertEquals("jan_kowalski", user.getUsername());
        assertEquals("jan@example.com", user.getEmail());
        assertEquals("secret123", user.getPasswordHash());
    }

    @Test
    void testMapFromNullAndMap() {
        assertNull(dbObjectMapper.map(null, User.class));

        User fromMap = dbObjectMapper.map(
                Map.of("username", "anna", "email", "anna@example.com"),
                User.class
        );
        assertNotNull(fromMap);
        assertEquals("anna", fromMap.getUsername());
        assertEquals("anna@example.com", fromMap.getEmail());
    }

    @Test
    void testMapListWithNullOrEmptyJsonAgg() {
        var nullResult = dbObjectMapper.mapList(null, User.class);
        assertNotNull(nullResult);
        assertTrue(nullResult.isEmpty());

        var emptyStringResult = dbObjectMapper.mapList("   ", User.class);
        assertNotNull(emptyStringResult);
        assertTrue(emptyStringResult.isEmpty());

        var literalNullStringResult = dbObjectMapper.mapList("null", User.class);
        assertNotNull(literalNullStringResult);
        assertTrue(literalNullStringResult.isEmpty());

        var emptyArrayResult = dbObjectMapper.mapList("[]", User.class);
        assertNotNull(emptyArrayResult);
        assertTrue(emptyArrayResult.isEmpty());
    }

    @Test
    void testMapListWithJsonAggString() {
        String dbJsonAgg = """
                [
                    {"id":1,"username":"admin","email":"admin@example.com","role":"ROLE_ADMIN","enabled":true},
                    {"id":2,"username":"jan","email":"jan@example.com","role":"ROLE_USER","enabled":true}
                ]
                """;

        var users = dbObjectMapper.mapList(dbJsonAgg, User.class);

        assertNotNull(users);
        assertEquals(2, users.size());
        assertEquals("admin", users.get(0).getUsername());
        assertEquals("ROLE_ADMIN", users.get(0).getRole());
        assertEquals("jan", users.get(1).getUsername());
        assertEquals("ROLE_USER", users.get(1).getRole());
    }

    @Test
    void testMapThrowsDbMappingExceptionOnMalformedJson() {
        DbMappingException ex = assertThrows(DbMappingException.class,
                () -> dbObjectMapper.map("{invalid-json", User.class));

        assertEquals("Failed to map payload to User", ex.getMessage());
        assertNotNull(ex.getCause());
    }

    @Test
    void testMapListThrowsDbMappingExceptionOnMalformedJson() {
        DbMappingException ex = assertThrows(DbMappingException.class,
                () -> dbObjectMapper.mapList("[{invalid-json", User.class));

        assertEquals("Failed to map payload to List<User>", ex.getMessage());
        assertNotNull(ex.getCause());
    }
}

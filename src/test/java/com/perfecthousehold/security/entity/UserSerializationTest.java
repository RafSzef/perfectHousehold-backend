package com.perfecthousehold.security.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.perfecthousehold.config.JacksonConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserSerializationTest {

    private final ObjectMapper objectMapper = new JacksonConfig().objectMapper();

    @Test
    void testPasswordHashIsNeverSerializedToResponseJson() throws Exception {
        User user = new User();
        user.setId(10L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPasswordHash("$2a$10$supersecretpasswordhash");
        user.setRole("ROLE_USER");
        user.setEnabled(true);

        String json = objectMapper.writeValueAsString(user);

        assertFalse(json.contains("passwordHash"), "passwordHash must NOT be serialized in output JSON");
        assertFalse(json.contains("supersecretpasswordhash"), "Secret hash value must never leak");
        assertTrue(json.contains("\"username\":\"testuser\""));
        assertTrue(json.contains("\"email\":\"test@example.com\""));
    }

    @Test
    void testPasswordHashIsDeserializedFromIncomingRequestJson() throws Exception {
        String incomingJson = """
                {
                    "username": "nowy_user",
                    "email": "nowy@example.com",
                    "passwordHash": "surowe_haslo_z_requestu"
                }
                """;

        User user = objectMapper.readValue(incomingJson, User.class);

        assertNotNull(user);
        assertEquals("nowy_user", user.getUsername());
        assertEquals("surowe_haslo_z_requestu", user.getPasswordHash());
    }
}

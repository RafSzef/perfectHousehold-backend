package com.perfecthousehold.security.entity;

import lombok.Data;

@Data
public class User {

    private Long id;
    private String username;
    private String email;
    private String passwordHash;
    private String role;
    private boolean enabled;

}

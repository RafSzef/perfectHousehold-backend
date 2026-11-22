package com.perfecthousehold.security.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.perfecthousehold.transport.view.Views;
import lombok.Data;

@Data
public class User {

    @JsonView(Views.Public.class)
    private Long id;

    @JsonView(Views.Public.class)
    private String username;

    @JsonView(Views.Public.class)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passwordHash;

    @JsonView(Views.Public.class)
    private String role;

    @JsonView(Views.Public.class)
    private boolean enabled;

}

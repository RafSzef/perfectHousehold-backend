package com.perfecthousehold.security.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.perfecthousehold.transport.view.Views;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.groups.Default;
import lombok.Data;

@Data
public class User {

    public interface OnCreate extends Default {
    }

    @JsonView(Views.Public.class)
    private Long id;

    @NotBlank(message = "Username must not be blank")
    @JsonView(Views.Public.class)
    private String username;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be valid")
    @JsonView(Views.Public.class)
    private String email;

    @NotBlank(groups = OnCreate.class, message = "Password must not be blank")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passwordHash;

    @JsonView(Views.Public.class)
    private String role;

    @JsonView(Views.Public.class)
    private boolean enabled;

}

package io.roadmap.filestorage.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Data Transfer Object for user registration and authentication
 */
@Data
public class UserDTO {

    /**
     * User's unique identifier for login
     * - Must be between 6 and 30 characters long
     * - Can only contain letters (A-Z, a-z), digits (0-9), underscore (_) and hyphen (-)
     * - Cannot contain spaces or special characters
     */
    @NotNull(message = "username is required")
    @Pattern(regexp = "^[0-9A-Za-z_-]+$",
            message = "username may only contain letters, digits, underscore (_) and hyphen (-)")
    @Size(min = 6, max = 30,
            message = "username must be between 6 and 30 characters long")
    private String username;

    /**
     * User's secret password for authentication
     * - Must be between 6 and 20 characters long
     * - Must contain at least one digit (0-9)
     * - Must contain at least one uppercase letter (A-Z)
     * - Must contain at least one lowercase letter (a-z)
     * - Can contain special characters
     */
    @NotNull(message = "password is required")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])[0-9A-Za-z!\"#$%&'()*+,\\-./:;<=>?@[\\\\]^_{|}~]+$",
            message = "password must contain at least one digit, one uppercase letter, and one lowercase letter")
    @Size(min = 6, max = 20,
            message = "password must be between 6 and 20 characters long")
    private String password;
}
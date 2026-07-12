package io.roadmap.filestorage.dtos;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

public record RegisterDTO(
        @NotNull(message = "username is required")
        @Pattern(regexp = "^[0-9A-Za-z_-]+$",
                message = "username may only contain letters, digits, underscore (_) and hyphen (-)")
        @Size(min = 6, max = 30,
                message = "username must be between 6 and 30 characters long")
        String username,


        @NotNull(message = "password is required")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])[0-9A-Za-z!\"#$%&'()*+,\\-./:;<=>?@[\\\\]^_{|}~]+$",
                message = "password must contain at least one digit, one uppercase letter, and one lowercase letter")
        @Size(min = 6, max = 20,
                message = "password must be between 6 and 20 characters long")
        String password

) {
}
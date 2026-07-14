package io.roadmap.filestorage.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Данные для регистрации нового пользователя")
public record RegisterDTO(
        @Schema(description = "Логин: 6–30 символов, только буквы, цифры, `_` и `-`",
                example = "john_doe", minLength = 6, maxLength = 30, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "username is required")
        @Pattern(regexp = "^[0-9A-Za-z_-]+$",
                message = "username may only contain letters, digits, underscore (_) and hyphen (-)")
        @Size(min = 6, max = 30,
                message = "username must be between 6 and 30 characters long")
        String username,


        @Schema(description = "Пароль: 6–20 символов, минимум одна цифра, одна заглавная и одна строчная буква",
                example = "Secret123", minLength = 6, maxLength = 20,
                requiredMode = Schema.RequiredMode.REQUIRED, format = "password")
        @NotNull(message = "password is required")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])[0-9A-Za-z!\"#$%&'()*+,\\-./:;<=>?@[\\\\]^_{|}~]+$",
                message = "password must contain at least one digit, one uppercase letter, and one lowercase letter")
        @Size(min = 6, max = 20,
                message = "password must be between 6 and 20 characters long")
        String password

) {
}
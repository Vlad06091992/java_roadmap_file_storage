package io.roadmap.filestorage.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Учётные данные для входа")
public record LoginDTO(
        @Schema(description = "Логин пользователя", example = "john_doe",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String username,

        @Schema(description = "Пароль пользователя", example = "Secret123",
                requiredMode = Schema.RequiredMode.REQUIRED, format = "password")
        String password
) {
}

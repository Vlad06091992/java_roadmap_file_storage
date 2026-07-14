package io.roadmap.filestorage.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ на вход/регистрацию/запрос текущего пользователя")
public record RegisterOrLoginResponseDTO(
        @Schema(description = "Имя аутентифицированного пользователя", example = "john_doe")
        String username
) { }

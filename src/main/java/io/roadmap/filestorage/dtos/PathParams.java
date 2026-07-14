package io.roadmap.filestorage.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Путь к папке")
public record PathParams(
        @Schema(description = "Путь к папке относительно корня пользователя. Пустая строка — корень.",
                example = "docs/reports", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Pattern(regexp = "^$|^[a-zA-Zа-яА-Я0-9_. -]+(?:/[a-zA-Zа-яА-Я0-9_. -]+)*/?$",
                message = "Некорректный путь")
        String path
) { }

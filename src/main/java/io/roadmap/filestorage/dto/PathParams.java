package io.roadmap.filestorage.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record PathParams(
        @NotNull
        @Pattern(regexp = "^[a-zA-Zа-яА-Я0-9_.-]+(/[a-zA-Zа-яА-Я0-9_.-]+)*/?$",
                message = "Некорректный путь")
        String path
) { }

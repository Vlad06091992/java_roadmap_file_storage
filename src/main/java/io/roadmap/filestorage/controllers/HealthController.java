package io.roadmap.filestorage.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Служебное", description = "Проверка доступности сервиса.")
@RestController
@RequestMapping("/api")
public class HealthController {

    @Operation(summary = "Health-check",
            description = "Возвращает статус приложения. Требует активной сессии (эндпоинт находится за аутентификацией).")
    @ApiResponse(responseCode = "200", description = "Сервис работает")
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UPP!");
    }
}

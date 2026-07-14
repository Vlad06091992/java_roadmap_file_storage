package io.roadmap.filestorage.configs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация OpenAPI/Swagger.
 * <p>
 * Swagger UI:      http://localhost:8081/swagger-ui.html
 * OpenAPI-схема:   http://localhost:8081/v3/api-docs
 * <p>
 * Аутентификация в приложении сессионная: после {@code /api/auth/sign-in} или
 * {@code /api/auth/sign-up} сервер выставляет cookie {@code SESSION} (Spring Session + Redis),
 * которую браузер автоматически прикрепляет к последующим запросам, в том числе из Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    /** Имя схемы безопасности, на которое ссылаются контроллеры. */
    public static final String SESSION_COOKIE = "sessionCookie";

    @Bean
    public OpenAPI fileStorageOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cloud File Storage API")
                        .version("v1")
                        .description("""
                                REST API облачного файлового хранилища.

                                Объектное хранилище — MinIO (S3-совместимое), метаданные пользователей — PostgreSQL,
                                сессии — Redis (Spring Session).

                                Аутентификация сессионная: залогиньтесь через `POST /api/auth/sign-in`
                                (или зарегистрируйтесь через `POST /api/auth/sign-up`) — сервер выставит cookie
                                `SESSION`, после чего защищённые эндпоинты станут доступны прямо из Swagger UI.

                                Пути к ресурсам передаются как URL-кодированные query-параметры; корень
                                пользователя — пустой путь `""`.""")
                        .contact(new Contact()
                                .name("FileStorage")
                                .url("https://github.com/"))
                        .license(new License().name("MIT")))
                .addServersItem(new Server().url("/").description("Текущий сервер"))
                .components(new Components()
                        .addSecuritySchemes(SESSION_COOKIE, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("SESSION")
                                .description("""
                                        Сессионная cookie Spring Session (хранится в Redis).
                                        Выставляется автоматически после успешного входа/регистрации;
                                        браузер прикрепляет её к запросам того же origin.""")))
                // По умолчанию все операции защищены сессионной cookie;
                // публичные эндпоинты снимают требование через @SecurityRequirements.
                .addSecurityItem(new SecurityRequirement().addList(SESSION_COOKIE));
    }
}

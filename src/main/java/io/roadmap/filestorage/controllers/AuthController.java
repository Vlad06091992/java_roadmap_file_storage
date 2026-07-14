package io.roadmap.filestorage.controllers;

import io.roadmap.filestorage.dtos.LoginDTO;
import io.roadmap.filestorage.dtos.RegisterDTO;
import io.roadmap.filestorage.dtos.RegisterOrLoginResponseDTO;
import io.roadmap.filestorage.entities.User;
import io.roadmap.filestorage.services.AuthService;
import io.roadmap.filestorage.services.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.roadmap.filestorage.configs.OpenApiConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "Аутентификация", description = "Регистрация, вход и выход. Управляет сессионной cookie SESSION.")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final ResourceService resourceService;
    private final SecurityContextRepository securityContextRepository;
    private final SecurityContextLogoutHandler securityContextLogoutHandler;

    private void loginAndSaveContext(LoginDTO loginRequest,HttpServletRequest request, HttpServletResponse response){
        SecurityContext context = authService.login(loginRequest);
        securityContextRepository.saveContext(context, request, response);
    }

    @Operation(
            summary = "Вход в систему",
            description = "Проверяет учётные данные и открывает сессию: в ответе выставляется cookie SESSION. "
                    + "Публичный эндпоинт — аутентификация не требуется.")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешный вход, сессия открыта"),
            @ApiResponse(responseCode = "401", description = "Неверный логин или пароль",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/sign-in")
    public RegisterOrLoginResponseDTO login(@RequestBody LoginDTO loginRequest, HttpServletRequest request, HttpServletResponse response) {
        loginAndSaveContext(loginRequest,request,response);
        RegisterOrLoginResponseDTO registerResponseDTO = new RegisterOrLoginResponseDTO(loginRequest.username());
        return registerResponseDTO;

    }

    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создаёт пользователя, заводит его корневую папку в хранилище и сразу открывает сессию "
                    + "(в ответе выставляется cookie SESSION). Публичный эндпоинт.")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Пользователь создан, сессия открыта"),
            @ApiResponse(responseCode = "400", description = "Невалидные данные (нарушены ограничения логина/пароля)",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Пользователь с таким именем уже существует",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/sign-up")
    public ResponseEntity<RegisterOrLoginResponseDTO> register(@Valid @RequestBody RegisterDTO registerDTO, HttpServletRequest request, HttpServletResponse response) {
        User user = authService.register(registerDTO);
        resourceService.createUserRootFolder(user.getId());
        RegisterOrLoginResponseDTO registerResponseDTO = new RegisterOrLoginResponseDTO(user.getUsername());
        LoginDTO loginRequest = new LoginDTO(registerDTO.username(), registerDTO.password());
        loginAndSaveContext(loginRequest,request,response);
        return new ResponseEntity(registerResponseDTO, HttpStatus.CREATED);
    }


    @Operation(
            summary = "Выход из системы",
            description = "Завершает текущую сессию и очищает контекст безопасности.")
    @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Сессия завершена"),
            @ApiResponse(responseCode = "401", description = "Нет активной сессии",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/sign-out")
    public ResponseEntity<Void> performLogout(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        securityContextLogoutHandler.logout(request, response, authentication);
        return ResponseEntity.noContent().build();
    }
}

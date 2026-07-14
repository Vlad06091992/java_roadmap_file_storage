package io.roadmap.filestorage.controllers;

import io.roadmap.filestorage.dtos.RegisterOrLoginResponseDTO;
import io.roadmap.filestorage.entities.User;
import io.roadmap.filestorage.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Пользователь", description = "Информация о текущем аутентифицированном пользователе.")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @Operation(
            summary = "Текущий пользователь",
            description = "Возвращает имя пользователя, которому принадлежит активная сессия.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Данные текущего пользователя"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/me")
    public ResponseEntity<RegisterOrLoginResponseDTO> me() {
        User user = authService.getCurrentUser();
        RegisterOrLoginResponseDTO registerResponseDTO = new RegisterOrLoginResponseDTO(user.getUsername());
        return new ResponseEntity(registerResponseDTO, HttpStatus.OK);
    }

}

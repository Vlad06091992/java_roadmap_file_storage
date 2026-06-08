package io.roadmap.filestorage.controller;

import io.roadmap.filestorage.dto.LoginDTO;
import io.roadmap.filestorage.dto.RegisterDTO;
import io.roadmap.filestorage.dto.RegisterOrLoginResponseDTO;
import io.roadmap.filestorage.entity.User;
import io.roadmap.filestorage.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/sign-in")
    public RegisterOrLoginResponseDTO login(@RequestBody LoginDTO loginRequest, HttpServletRequest request, HttpServletResponse response) {
        authService.login(loginRequest);
        RegisterOrLoginResponseDTO registerResponseDTO = new RegisterOrLoginResponseDTO(loginRequest.username());
        return registerResponseDTO;

    }

    @PostMapping("/sign-up")
    public ResponseEntity<RegisterOrLoginResponseDTO> register(@Valid @RequestBody RegisterDTO registerDTO, HttpServletRequest request, HttpServletResponse response) {
        User user = authService.register(registerDTO);
        RegisterOrLoginResponseDTO registerResponseDTO = new RegisterOrLoginResponseDTO(user.getUsername());
        return new ResponseEntity(registerResponseDTO,HttpStatus.CREATED);
    }
}

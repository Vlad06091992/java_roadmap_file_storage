package io.roadmap.filestorage.controllers;

import io.roadmap.filestorage.dtos.RegisterOrLoginResponseDTO;
import io.roadmap.filestorage.entities.User;
import io.roadmap.filestorage.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    @GetMapping("/me")
    public ResponseEntity<RegisterOrLoginResponseDTO> me() {
        User user = authService.getCurrentUser();
        RegisterOrLoginResponseDTO registerResponseDTO = new RegisterOrLoginResponseDTO(user.getUsername());
        return new ResponseEntity(registerResponseDTO, HttpStatus.OK);
    }

}

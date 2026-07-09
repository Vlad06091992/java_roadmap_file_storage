package io.roadmap.filestorage.controller;

import io.roadmap.filestorage.dto.RegisterOrLoginResponseDTO;
import io.roadmap.filestorage.entity.User;
import io.roadmap.filestorage.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

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

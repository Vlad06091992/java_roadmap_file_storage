package io.roadmap.filestorage.controllers;

import io.roadmap.filestorage.dtos.LoginDTO;
import io.roadmap.filestorage.dtos.RegisterDTO;
import io.roadmap.filestorage.dtos.RegisterOrLoginResponseDTO;
import io.roadmap.filestorage.entities.User;
import io.roadmap.filestorage.services.AuthService;
import io.roadmap.filestorage.services.ResourceService;
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

    @PostMapping("/sign-in")
    public RegisterOrLoginResponseDTO login(@RequestBody LoginDTO loginRequest, HttpServletRequest request, HttpServletResponse response) {
        loginAndSaveContext(loginRequest,request,response);
        RegisterOrLoginResponseDTO registerResponseDTO = new RegisterOrLoginResponseDTO(loginRequest.username());
        return registerResponseDTO;

    }

    @PostMapping("/sign-up")
    public ResponseEntity<RegisterOrLoginResponseDTO> register(@Valid @RequestBody RegisterDTO registerDTO, HttpServletRequest request, HttpServletResponse response) {
        User user = authService.register(registerDTO);
        resourceService.createUserRootFolder(user.getId());
        RegisterOrLoginResponseDTO registerResponseDTO = new RegisterOrLoginResponseDTO(user.getUsername());
        LoginDTO loginRequest = new LoginDTO(registerDTO.username(), registerDTO.password());
        loginAndSaveContext(loginRequest,request,response);
        return new ResponseEntity(registerResponseDTO, HttpStatus.CREATED);
    }


    @PostMapping("/sign-out")
    public ResponseEntity<Void> performLogout(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        securityContextLogoutHandler.logout(request, response, authentication);
        return ResponseEntity.noContent().build();
    }
}

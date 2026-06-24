package io.roadmap.filestorage.controller;

import io.roadmap.filestorage.dto.RegisterOrLoginResponseDTO;
import io.roadmap.filestorage.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

//    @GetMapping("/me")
//    public ResponseEntity<RegisterOrLoginResponseDTO> me(Authentication authentication) {
//        String username = "";
//        Optional<Object> principal = Optional.of(authentication.getPrincipal());
//
//        if(principal.isPresent()){
//            User user = (User) principal.get();
//            username = user.getUsername();
//        }
//
//        RegisterOrLoginResponseDTO registerResponseDTO = new RegisterOrLoginResponseDTO(username);
//        return new ResponseEntity(registerResponseDTO, HttpStatus.OK);
//    }

    //TODO разобраться, как еще можно получить юзера, например @AuthenticationPrincipal
    @GetMapping("/me")
    public ResponseEntity<RegisterOrLoginResponseDTO> me(Authentication authentication) {
        String username = "";
        Optional<Object> principal = Optional.of(authentication.getPrincipal());

        if(principal.isPresent()){
            User user = (User) principal.get();
            username = user.getUsername();
        }

        RegisterOrLoginResponseDTO registerResponseDTO = new RegisterOrLoginResponseDTO(username);
        return new ResponseEntity(registerResponseDTO, HttpStatus.OK);
    }

}

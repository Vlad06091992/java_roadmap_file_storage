package io.roadmap.filestorage.services;

import io.roadmap.filestorage.dto.LoginDTO;
import io.roadmap.filestorage.dto.RegisterDTO;
import io.roadmap.filestorage.entity.User;
import io.roadmap.filestorage.exceptions.UserAlreadyExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;

    public Object login(LoginDTO loginRequest) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                loginRequest.username(), loginRequest.password());

        System.out.println(token);

        Authentication authentication = authenticationManager.authenticate(token);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        return null;
    }


    public User register(RegisterDTO registerDTO) {
        User user = new User();

        Boolean isExist = userDetailsManager.userExists(registerDTO.username());

        if (isExist) {
            throw new UserAlreadyExistException();
        }

        user.setPassword(passwordEncoder.encode(registerDTO.password()));
        user.setUsername(registerDTO.username());
        userDetailsManager.createUser(user);

        return user;
    }

}


package io.roadmap.filestorage.services;

import io.roadmap.filestorage.dtos.LoginDTO;
import io.roadmap.filestorage.dtos.RegisterDTO;
import io.roadmap.filestorage.entities.Bucket;
import io.roadmap.filestorage.entities.User;
import io.roadmap.filestorage.exceptions.UnauthorizedException;
import io.roadmap.filestorage.exceptions.UserAlreadyExistException;
import io.roadmap.filestorage.repositories.BucketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;
    private final BucketRepository bucketRepository;

    public SecurityContext login(LoginDTO loginRequest) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                loginRequest.username(), loginRequest.password());

        Authentication authentication = authenticationManager.authenticate(token);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        return context;
    }


    public User register(RegisterDTO registerDTO) {
        // 1. Сначала проверяем существование
        if (userDetailsManager.userExists(registerDTO.username())) {
            throw new UserAlreadyExistException();
        }

        try {
            User user = new User();
            user.setUsername(registerDTO.username());
            user.setPassword(passwordEncoder.encode(registerDTO.password()));

            //TODO унести в какие-то утилиты создание имени бакета, и использовать унитарно
            String bucketName = registerDTO.username().toLowerCase();

            Bucket bucket = new Bucket();
            bucket.setName(bucketName);
            bucket.setUser(user);
            bucketRepository.save(bucket);

            user.setBucket(bucket);
            userDetailsManager.createUser(user);

            return user;

        } catch (Exception e) {
            // Пробрасываем оригинальное исключение для отката транзакции
            throw new RuntimeException(e);
        }
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException();
        }
        return (User) authentication.getPrincipal();
    }
}


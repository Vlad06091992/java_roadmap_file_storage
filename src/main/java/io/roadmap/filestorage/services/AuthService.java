package io.roadmap.filestorage.services;

import io.roadmap.filestorage.dto.LoginDTO;
import io.roadmap.filestorage.dto.RegisterDTO;
import io.roadmap.filestorage.entity.Bucket;
import io.roadmap.filestorage.entity.User;
import io.roadmap.filestorage.exceptions.UserAlreadyExistException;
import io.roadmap.filestorage.repositories.BucketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

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
//            String bucketName = sanitizeBucketName(registerDTO.username());
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
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
        }
    }

    private String sanitizeBucketName(String username) {
        return username.toLowerCase()
                .replaceAll("[^a-z0-9-]", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "")
                + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        return (User) authentication.getPrincipal();
    }

}


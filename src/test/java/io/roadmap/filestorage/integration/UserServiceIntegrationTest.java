package io.roadmap.filestorage.integration;

import io.roadmap.filestorage.dtos.RegisterDTO;
import io.roadmap.filestorage.entities.User;
import io.roadmap.filestorage.exceptions.UserAlreadyExistException;
import io.roadmap.filestorage.repositories.UserRepository;
import io.roadmap.filestorage.services.AppUserDetailsManager;
import io.roadmap.filestorage.services.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Интеграционные тесты сервиса по работе с пользователями.
 * <p>
 * Тестируется связка слоя данных (JPA + {@link UserRepository}) с классами-сервисами,
 * отвечающими за пользователей ({@link AuthService}, {@link AppUserDetailsManager}).
 * <p>
 * Вместо H2 используется полноценный PostgreSQL, поднимаемый в Docker через Testcontainers.
 * Это приближает окружение тестов к рабочему и позволяет проверять нюансы, специфичные
 * для конкретного движка БД (например, работу UNIQUE-ограничений на уровне таблицы).
 * <p>
 * Схема таблицы {@code users} создаётся реальными Flyway-миграциями (как в production).
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({AuthService.class, AppUserDetailsManager.class, UserServiceIntegrationTest.TestBeans.class})
@TestPropertySource(properties = {
        // Схему строит Flyway; Hibernate только проверяет соответствие сущностей таблицам (как в prod).
        "spring.jpa.hibernate.ddl-auto=validate"
})
class UserServiceIntegrationTest {

    // В Testcontainers 2.0 PostgreSQLContainer больше не generic-класс.
    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private AuthService authService;

    @Autowired
    private AppUserDetailsManager userDetailsManager;

    @Autowired
    private UserRepository userRepository;

    /**
     * Вызов метода "создать пользователя" в сервисе приводит к появлению
     * новой записи в таблице users.
     */
    @Test
    void register_persistsNewUserRowInUsersTable() {
        assertThat(userRepository.count()).isZero();

        User created = authService.register(new RegisterDTO("john_doe", "Password1"));

        // Пользователь получил сгенерированный БД идентификатор.
        assertThat(created.getId()).isNotNull();

        // В таблице появилась ровно одна запись, и её можно найти по username.
        assertThat(userRepository.count()).isEqualTo(1);
        Optional<User> persisted = userRepository.findByUsername("john_doe");
        assertThat(persisted).isPresent();
        assertThat(persisted.get().getId()).isEqualTo(created.getId());
        assertThat(persisted.get().getCreatedAt()).isNotNull();
        // Пароль сохранён в зашифрованном (BCrypt) виде, а не как есть.
        assertThat(persisted.get().getPassword()).isNotEqualTo("Password1");
    }

    /**
     * Создание пользователя с неуникальным username приводит к ожидаемому
     * типу исключения ({@link UserAlreadyExistException}) на уровне сервиса.
     */
    @Test
    void register_withDuplicateUsername_throwsUserAlreadyExistException() {
        authService.register(new RegisterDTO("duplicate_user", "Password1"));

        assertThatThrownBy(() ->
                authService.register(new RegisterDTO("duplicate_user", "Password2")))
                .isInstanceOf(UserAlreadyExistException.class);

        // Вторая попытка не создала лишней записи.
        assertThat(userRepository.count()).isEqualTo(1);
    }

    /**
     * UNIQUE-ограничение существует и на уровне самой БД: попытка сохранить второго
     * пользователя с тем же username в обход сервисной проверки завершается
     * DataIntegrityViolationException (движок-специфичное поведение PostgreSQL).
     */
    @Test
    void repository_savingDuplicateUsername_violatesDatabaseUniqueConstraint() {
        userDetailsManager.createUser(newUser("unique_name", "encoded-pwd-1"));
        userRepository.flush();

        assertThatThrownBy(() -> {
            userDetailsManager.createUser(newUser("unique_name", "encoded-pwd-2"));
            userRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    private static User newUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        return user;
    }

    /**
     * Заглушки для бинов, которые нужны {@link AuthService}, но не участвуют в проверяемых
     * сценариях (метод register их не вызывает). AuthenticationManager используется только
     * при login, поэтому здесь достаточно пустышки.
     */
    @TestConfiguration
    static class TestBeans {

        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        AuthenticationManager authenticationManager() {
            return authentication -> authentication;
        }
    }
}

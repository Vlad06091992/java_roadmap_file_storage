package io.roadmap.filestorage.integration;

import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.roadmap.filestorage.components.PathResolver;
import io.roadmap.filestorage.dtos.interfaces.GetResourceData;
import io.roadmap.filestorage.entities.User;
import io.roadmap.filestorage.exceptions.ResourceNotFoundException;
import io.roadmap.filestorage.services.AuthService;
import io.roadmap.filestorage.services.ResourceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Интеграционные тесты сервиса по работе с файлами и папками ({@link ResourceService}).
 * <p>
 * Опциональное задание повышенной сложности — проверяется взаимодействие с реальным
 * хранилищем Minio, поднятым в Docker через {@link GenericContainer} (Testcontainers).
 * <p>
 * Права доступа завязаны на текущего пользователя из Spring Security: {@code ResourceService}
 * определяет "корневую папку" пользователя по id принципала из {@link SecurityContextHolder}.
 * Поэтому здесь интегрируется JUnit со Spring Security — контекст аутентификации выставляется
 * вручную перед каждым сценарием.
 */
@Testcontainers
class ResourceServiceIntegrationTest {

    private static final int MINIO_PORT = 9000;
    private static final String ACCESS_KEY = "minioadmin";
    private static final String SECRET_KEY = "minioadmin";

    @Container
    static final GenericContainer<?> MINIO =
            new GenericContainer<>(DockerImageName.parse("minio/minio:latest"))
                    .withExposedPorts(MINIO_PORT)
                    .withEnv("MINIO_ROOT_USER", ACCESS_KEY)
                    .withEnv("MINIO_ROOT_PASSWORD", SECRET_KEY)
                    .withCommand("server", "/data")
                    .waitingFor(Wait.forHttp("/minio/health/ready")
                            .forPort(MINIO_PORT)
                            .withStartupTimeout(Duration.ofMinutes(2)));

    private static MinioClient minioClient;

    private ResourceService resourceService;

    @BeforeAll
    static void initClient() {
        String endpoint = "http://" + MINIO.getHost() + ":" + MINIO.getMappedPort(MINIO_PORT);
        minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(ACCESS_KEY, SECRET_KEY)
                .build();
    }

    @BeforeEach
    void setUp() {
        // ResourceService из зависимостей AuthService использует только getCurrentUser(),
        // который читает принципала из SecurityContextHolder, поэтому остальные зависимости не нужны.
        AuthService authService = new AuthService(null, null, null);
        resourceService = new ResourceService(authService, minioClient, new PathResolver());
        resourceService.init(); // создаёт bucket 'user-files', если он ещё не создан
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    // ---------------------------------------------------------------------
    // Загрузка / переименование / удаление
    // ---------------------------------------------------------------------

    /**
     * Загрузка файла приводит к его появлению в bucket'е Minio в корневой папке
     * текущего пользователя.
     */
    @Test
    void uploadFile_appearsInCurrentUsersRootFolder() throws Exception {
        User user = authenticateAsNewUser();

        resourceService.saveFile(null, new MultipartFile[]{file("hello.txt", "Hello, world!")});

        String expectedObject = userObject(user, "hello.txt");
        assertThat(objectExists(expectedObject)).isTrue();

        StatObjectResponse stat = stat(expectedObject);
        assertThat(stat.size()).isEqualTo("Hello, world!".getBytes(StandardCharsets.UTF_8).length);
    }

    /**
     * Переименование файла (move) перемещает объект: старого имени больше нет, новое появилось.
     */
    @Test
    void renameFile_movesObjectToNewName() throws Exception {
        User user = authenticateAsNewUser();
        resourceService.saveFile(null, new MultipartFile[]{file("old-name.txt", "payload")});

        resourceService.move("old-name.txt", "new-name.txt");

        assertThat(objectExists(userObject(user, "old-name.txt"))).isFalse();
        assertThat(objectExists(userObject(user, "new-name.txt"))).isTrue();
    }

    /**
     * Удаление файла приводит к исчезновению объекта из bucket'а.
     */
    @Test
    void deleteFile_removesObject() throws Exception {
        User user = authenticateAsNewUser();
        resourceService.saveFile(null, new MultipartFile[]{file("temp.txt", "payload")});
        assertThat(objectExists(userObject(user, "temp.txt"))).isTrue();

        resourceService.remove("temp.txt");

        assertThat(objectExists(userObject(user, "temp.txt"))).isFalse();
    }

    /**
     * Переименование папки перемещает вместе с ней вложенные файлы.
     */
    @Test
    void renameFolder_movesFolderWithItsContent() throws Exception {
        User user = authenticateAsNewUser();
        resourceService.createFolder("projects");
        resourceService.saveFile("projects", new MultipartFile[]{file("notes.txt", "content")});

        resourceService.move("projects/", "archive/");

        assertThat(objectExists(userObject(user, "archive/notes.txt"))).isTrue();
        assertThat(objectExists(userObject(user, "projects/notes.txt"))).isFalse();
    }

    /**
     * Удаление папки удаляет все вложенные в неё объекты.
     */
    @Test
    void deleteFolder_removesAllContainedObjects() throws Exception {
        authenticateAsNewUser();
        resourceService.createFolder("docs");
        resourceService.saveFile("docs", new MultipartFile[]{
                file("a.txt", "first"),
                file("b.txt", "second")
        });
        assertThat(resourceService.isExistResource("docs/")).isTrue();

        resourceService.remove("docs/");

        assertThat(resourceService.isExistResource("docs/")).isFalse();
    }

    // ---------------------------------------------------------------------
    // Права доступа и "поиск"
    // ---------------------------------------------------------------------

    /**
     * Проверка прав доступа: пользователь не имеет доступа к чужим файлам —
     * они не видны и запрос к ним завершается ResourceNotFoundException,
     * тогда как владелец свой файл видит.
     */
    @Test
    void accessControl_userCannotAccessAnotherUsersFile() throws Exception {
        User owner = authenticateAsNewUser();
        resourceService.saveFile(null, new MultipartFile[]{file("secret.txt", "top secret")});

        // Другой пользователь: файл в его пространстве отсутствует.
        authenticateAsNewUser();
        assertThat(resourceService.isExistResource("secret.txt")).isFalse();
        assertThatThrownBy(() -> resourceService.getResourceData("secret.txt"))
                .isInstanceOf(ResourceNotFoundException.class);

        // Владелец по-прежнему видит свой файл.
        authenticateAs(owner);
        assertThat(resourceService.isExistResource("secret.txt")).isTrue();
    }

    /**
     * Поиск/просмотр содержимого: пользователь находит только свои файлы,
     * но не файлы других пользователей.
     */
    @Test
    void listing_userSeesOwnFilesButNotOthers() throws Exception {
        User alice = authenticateAsNewUser();
        resourceService.saveFile(null, new MultipartFile[]{
                file("report.txt", "r"),
                file("photo.txt", "p")
        });

        User bob = authenticateAs(newUser());
        resourceService.saveFile(null, new MultipartFile[]{file("bob-secret.txt", "s")});

        // Bob видит только свой файл.
        assertThat(namesInRoot()).containsExactlyInAnyOrder("bob-secret.txt");

        // Alice видит только свои файлы.
        authenticateAs(alice);
        assertThat(namesInRoot()).containsExactlyInAnyOrder("report.txt", "photo.txt");
    }

    // ---------------------------------------------------------------------
    // Вспомогательные методы
    // ---------------------------------------------------------------------

    private List<String> namesInRoot() {
        return resourceService.getFolderData("").stream()
                .map(GetResourceData::name)
                .toList();
    }

    private MultipartFile file(String name, String content) {
        return new MockMultipartFile("object", name, "text/plain",
                content.getBytes(StandardCharsets.UTF_8));
    }

    private String userObject(User user, String relativePath) {
        return "user-" + user.getId() + "-files/" + relativePath;
    }

    private StatObjectResponse stat(String objectName) throws Exception {
        return minioClient.statObject(StatObjectArgs.builder()
                .bucket(ResourceService.BUCKET_NAME)
                .object(objectName)
                .build());
    }

    private boolean objectExists(String objectName) {
        try {
            stat(objectName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static User newUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("user-" + user.getId());
        user.setPassword("irrelevant");
        return user;
    }

    private User authenticateAs(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList()));
        return user;
    }

    private User authenticateAsNewUser() {
        return authenticateAs(newUser());
    }
}

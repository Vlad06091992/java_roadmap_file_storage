package io.roadmap.filestorage.controllers;

import io.roadmap.filestorage.dtos.interfaces.GetResourceData;
import io.roadmap.filestorage.services.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;


@Tag(name = "Ресурсы", description = "Операции над файлами и папками: загрузка, удаление, просмотр, скачивание, перемещение/переименование.")
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class ResourceController {
    private final ResourceService resourceService;

    @Operation(
            summary = "Загрузить файлы",
            description = "Загружает один или несколько файлов (multipart/form-data) в папку `path`. "
                    + "Если `path` не указан — файлы попадают в корень пользователя.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Файлы загружены"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/resource")
    public ResponseEntity<Object> createDirectory(
            @Parameter(description = "Загружаемые файлы", required = true)
            @RequestParam("object") MultipartFile[] multipartFiles,
            @Parameter(description = "Папка назначения, например `docs/reports/`. Пусто — корень пользователя.",
                    example = "docs/reports/")
            @RequestParam(value = "path", required = false) String path) throws Exception {
        resourceService.saveFile(path, multipartFiles);
        return new ResponseEntity<>("", HttpStatus.CREATED);
    }

    @Operation(
            summary = "Удалить ресурс",
            description = "Удаляет файл или папку по пути `path`. Для папки удаление рекурсивное.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ресурс удалён"),
            @ApiResponse(responseCode = "404", description = "Ресурс не найден",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/resource")
    public ResponseEntity<Void> remove(
            @Parameter(description = "Путь к файлу или папке", required = true, example = "docs/report.pdf")
            @RequestParam String path) {
        resourceService.remove(path);
        return ResponseEntity.noContent().build();
    }


    @Operation(
            summary = "Информация о ресурсе",
            description = "Возвращает метаданные файла или папки: путь, имя, тип и (для файла) размер в байтах.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Метаданные ресурса"),
            @ApiResponse(responseCode = "404", description = "Ресурс не найден",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/resource")
    public ResponseEntity<GetResourceData> getDirectoryInfo(
            @Parameter(description = "Путь к файлу или папке", required = true, example = "docs/report.pdf")
            @RequestParam("path") String path) {
        GetResourceData getDirectoryDTO = resourceService.generateResponseData(path);
        return new ResponseEntity<>(getDirectoryDTO, HttpStatus.OK);
    }

    @Operation(
            summary = "Скачать ресурс",
            description = "Отдаёт содержимое файла (папка отдаётся архивом) потоком "
                    + "`application/octet-stream` с заголовком Content-Disposition: attachment.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Бинарный поток файла",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)),
            @ApiResponse(responseCode = "404", description = "Ресурс не найден",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/resource/download")
    public ResponseEntity<Object> download(
            @Parameter(description = "Путь к скачиваемому ресурсу", required = true, example = "docs/report.pdf")
            @RequestParam("path") String path) throws Exception {
        InputStream result = resourceService.getDownloadData(path);
        InputStreamResource resource = new InputStreamResource(result);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment")
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
    }

    @Operation(
            summary = "Переместить или переименовать ресурс",
            description = "Перемещает ресурс из `from` в `to`. Смена имени в конце пути = переименование. "
                    + "Возвращает метаданные ресурса на новом месте.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ресурс перемещён, возвращены его новые метаданные"),
            @ApiResponse(responseCode = "404", description = "Исходный ресурс не найден",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/resource/move")
    public ResponseEntity<GetResourceData> move(
            @Parameter(description = "Исходный путь", required = true, example = "docs/report.pdf")
            @RequestParam("from") String from,
            @Parameter(description = "Целевой путь", required = true, example = "archive/report-2026.pdf")
            @RequestParam("to") String to
    ) {
        resourceService.move(from, to);
        GetResourceData getDirectoryDTO = resourceService.generateResponseData(to);
        return ResponseEntity.ok()
                .body(getDirectoryDTO);
    }
}

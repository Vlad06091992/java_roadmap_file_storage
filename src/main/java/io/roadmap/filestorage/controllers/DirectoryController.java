package io.roadmap.filestorage.controllers;

import io.minio.messages.Item;
import io.roadmap.filestorage.dtos.GetDirectoryDTO;
import io.roadmap.filestorage.dtos.GetFileDTO;
import io.roadmap.filestorage.dtos.PathParams;
import io.roadmap.filestorage.dtos.interfaces.GetResourceData;
import io.roadmap.filestorage.services.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@Tag(name = "Папки", description = "Создание папок и просмотр их содержимого.")
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class DirectoryController {
    private final ResourceService resourceService;

    @Operation(
            summary = "Создать папку",
            description = "Создаёт пустую папку по пути `path`. Возвращает метаданные созданной папки.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Папка создана"),
            @ApiResponse(responseCode = "400", description = "Некорректный путь",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "Папка уже существует",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/directory")
    public ResponseEntity<Object>createDirectory (@Valid @ModelAttribute PathParams params) throws Exception {
        String path = params.path();
        GetDirectoryDTO getDirectoryDTO =  resourceService.createFolder(path);
        return new ResponseEntity<>(getDirectoryDTO, HttpStatus.CREATED);
    }


    @Operation(
            summary = "Содержимое папки",
            description = "Возвращает список ресурсов (файлов и вложенных папок) в папке `path`. "
                    + "Пустой путь `\"\"` — корень пользователя.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список ресурсов папки"),
            @ApiResponse(responseCode = "400", description = "Некорректный путь",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Папка не найдена",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/directory")
    public ResponseEntity<List<GetResourceData>>getData (@Valid @ModelAttribute PathParams params) {
        String path = params.path();
        List<GetResourceData> data =  resourceService.getFolderData(path);

        return new ResponseEntity<>(data, HttpStatus.OK);
    }

}

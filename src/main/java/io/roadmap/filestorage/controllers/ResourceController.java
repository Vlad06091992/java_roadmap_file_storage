package io.roadmap.filestorage.controllers;

import io.roadmap.filestorage.dtos.interfaces.GetResourceData;
import io.roadmap.filestorage.services.ResourceService;
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


@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class ResourceController {
    private final ResourceService resourceService;

    @PostMapping("/resource")
    public ResponseEntity<Object> createDirectory(
            @RequestParam("object") MultipartFile[] multipartFiles,
            @RequestParam(value = "path", required = false) String path) throws Exception {
        resourceService.saveFile(path, multipartFiles);
        return new ResponseEntity<>("", HttpStatus.CREATED);
    }

    @DeleteMapping("/resource")
    public ResponseEntity<Void> remove(@RequestParam String path) {
        resourceService.remove(path);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/resource")
    public ResponseEntity<GetResourceData> getDirectoryInfo(@RequestParam("path") String path) {
        GetResourceData getDirectoryDTO = resourceService.generateResponseData(path);
        return new ResponseEntity<>(getDirectoryDTO, HttpStatus.OK);
    }

    @GetMapping("/resource/download")
    public ResponseEntity<Object> download(@RequestParam("path") String path) throws Exception {
        InputStream result = resourceService.getDownloadData(path);
        InputStreamResource resource = new InputStreamResource(result);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment")
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
    }

    @GetMapping("/resource/move")
    public ResponseEntity<GetResourceData> move(
            @RequestParam("from") String from,
            @RequestParam("to") String to
    ) {
        resourceService.move(from, to);
        GetResourceData getDirectoryDTO = resourceService.generateResponseData(to);
        return ResponseEntity.ok()
                .body(getDirectoryDTO);
    }
}

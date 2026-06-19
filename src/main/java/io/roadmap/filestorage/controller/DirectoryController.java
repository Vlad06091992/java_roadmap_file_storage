package io.roadmap.filestorage.controller;

import io.roadmap.filestorage.dto.GetDirectoryDTO;
import io.roadmap.filestorage.dto.PathParams;
import io.roadmap.filestorage.services.DirectoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;


@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class DirectoryController {
    private final DirectoryService directoryService;

    @PostMapping("/directory")
    public ResponseEntity<Object>createDirectory (@Valid @ModelAttribute PathParams params) throws Exception {
        String path = params.path();
        GetDirectoryDTO getDirectoryDTO =  directoryService.createFolder(path);
        return new ResponseEntity<>(getDirectoryDTO, HttpStatus.CREATED);
    }

    @GetMapping("/directory")
    public ResponseEntity<GetDirectoryDTO> getDirectoryInfo(@RequestParam("path") String path) {
        String[] parts = path.split("/");

        log.info("lengths: {}", parts.length);
        log.info("parts: {}", Arrays.toString(parts));

        String directoryName = parts[parts.length - 1];

        int lastSlash = path.lastIndexOf('/');
        String directoryPath = path.substring(0, lastSlash);

        log.info("directoryPath: {}, directoryName: {}", directoryPath, directoryName);

        GetDirectoryDTO getDirectoryDTO = new GetDirectoryDTO(directoryPath, directoryName);
        return new ResponseEntity<>(getDirectoryDTO, HttpStatus.CREATED);
    }
}

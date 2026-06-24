package io.roadmap.filestorage.controller;

import io.roadmap.filestorage.dto.CreateFolderParamDTO;
import io.roadmap.filestorage.dto.GetDirectoryDTO;
import io.roadmap.filestorage.services.DirectoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class DirectoryController {
    private final DirectoryService directoryService;

    @PostMapping("/directory")
    public ResponseEntity<Object>createDirectory (@Valid @ModelAttribute CreateFolderParamDTO params) throws Exception {
        String path = params.path();
        GetDirectoryDTO getDirectoryDTO =  directoryService.createFolder(path);
        return new ResponseEntity<>(getDirectoryDTO, HttpStatus.CREATED);
    }


}

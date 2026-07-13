package io.roadmap.filestorage.controllers;

import io.minio.messages.Item;
import io.roadmap.filestorage.dtos.GetDirectoryDTO;
import io.roadmap.filestorage.dtos.GetFileDTO;
import io.roadmap.filestorage.dtos.PathParams;
import io.roadmap.filestorage.dtos.interfaces.GetResourceData;
import io.roadmap.filestorage.services.ResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class DirectoryController {
    private final ResourceService resourceService;

    @PostMapping("/directory")
    public ResponseEntity<Object>createDirectory (@Valid @ModelAttribute PathParams params) throws Exception {
        String path = params.path();
        GetDirectoryDTO getDirectoryDTO =  resourceService.createFolder(path);
        return new ResponseEntity<>(getDirectoryDTO, HttpStatus.CREATED);
    }


    @GetMapping("/directory")
    public ResponseEntity<List<GetResourceData>>getData (@Valid @ModelAttribute PathParams params) {
        String path = params.path();
        List<GetResourceData> data =  resourceService.getFolderData(path);

        return new ResponseEntity<>(data, HttpStatus.OK);
    }

}

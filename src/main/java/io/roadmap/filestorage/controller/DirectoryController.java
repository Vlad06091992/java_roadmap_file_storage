package io.roadmap.filestorage.controller;

import io.minio.messages.Item;
import io.roadmap.filestorage.dto.GetDirectoryDTO;
import io.roadmap.filestorage.dto.GetFileDTO;
import io.roadmap.filestorage.dto.PathParams;
import io.roadmap.filestorage.dto.interfaces.GetResourceData;
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
    public ResponseEntity<Object>getData (@Valid @ModelAttribute PathParams params) {
        String path = params.path();
        List<Item> data =  resourceService.getFolderData(path);

        List<GetResourceData> d = data.stream()
                .map(i -> {
                    if (i.isDir()) {
                        return GetDirectoryDTO.fromFullPath(i.objectName());
                    } else {
                        return GetFileDTO.fromFullPath(i.objectName(), i.size());

                    }
                }
            )
                .filter(e -> !(e.name().length() < 1))
                .collect(Collectors.toList());
        return new ResponseEntity<>(d, HttpStatus.OK);
    }


}

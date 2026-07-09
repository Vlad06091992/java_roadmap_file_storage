package io.roadmap.filestorage.controller;

import io.minio.GetObjectResponse;
import io.roadmap.filestorage.components.PathResolver;
import io.roadmap.filestorage.dto.*;
import io.roadmap.filestorage.dto.interfaces.GetResourceData;
import io.roadmap.filestorage.services.ResourceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
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
    private final PathResolver pathResolver;

    @PostMapping("/resource")
    public ResponseEntity<Object> createDirectory(
//            @RequestParam("object") MultipartFile multipartFile,
            @RequestParam("object") MultipartFile[] multipartFiles,

                                                  @RequestParam(value = "path", required = false) String path,
                                                  HttpServletRequest request, HttpServletResponse response) throws Exception {
        //TODO папка загружается только с одним файлом
//        GetFileDTO res =  directoryService.saveFile(path, multipartFiles);
          resourceService.saveFile(path, multipartFiles);
        return new ResponseEntity<>("", HttpStatus.CREATED);
    }

    @GetMapping("/path-resolver")
    public Object getPathData(@Valid @ModelAttribute PathParams params) {
        String path = params.path();
        return pathResolver.getPathData(path);
    }

    @DeleteMapping("/resource")
    public ResponseEntity<Void> remove(@RequestParam String path) {


        resourceService.remove(path);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/resource")
    public ResponseEntity<GetResourceData> getDirectoryInfo(@RequestParam("path") String path, InputStream inputStream) {

        //dir = folder1/folder2/folder3/
        //file = folder1/folder2/text.txt

        Boolean isDirectory = (path.charAt(path.length() - 1)) == '/';

        String[] parts = path.split("/");
        String dirName = parts[parts.length - 1];
        String dirPath = parts.length > 1 ? path.substring(0, path.length() - 1) : "/";

        String directoryName = parts[parts.length - 1];

        int lastSlash = path.lastIndexOf('/');
        String directoryPath = (parts.length > 1 ? path.substring(0, lastSlash) : "");

        GetObjectResponse result = resourceService.getData(path);

        Headers headers = result.headers();
        String object = result.object();
//        log.info("headers: {}",headers);
//        log.info("object: {}",object);


        String size = headers.get("Content-Length");
//        log.info("size: {}",size);

        long s = Long.valueOf(size);

        String type = isDirectory ? "DIRECTORY" : "FILE";
        GetResourceData getDirectoryDTO = isDirectory ? new GetDirectoryDTO(dirPath, dirName) : new GetFileDTO(dirPath, dirName, s);

        log.info("Type: {}", getDirectoryDTO.type());

        return new ResponseEntity<>(getDirectoryDTO, HttpStatus.CREATED);
    }

    @GetMapping("/resource/download")
    public ResponseEntity<Object> download(@RequestParam("path") String path) throws Exception {
        //TODO папка скачивается пустым архивом

        InputStream result = resourceService.getDownloadData(path);

        InputStreamResource resource = new InputStreamResource(result);

        String name = "";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"")
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
    }

    @GetMapping("/resource/move")
    public ResponseEntity<Object> move(
            @RequestParam("from") String from,
            @RequestParam("to") String to
    ) throws Exception {
        GetObjectResponse result = resourceService.getObject(from);

        //TODO все возможные проверки на наличие файла, путей

        String name = result.object();
        Headers headers = result.headers();
        String size = headers.get("Content-Length");
        resourceService.saveFromStream(result, to, name, Long.valueOf(size));
        resourceService.remove(from);


        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + "" + "\"")
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(null);
    }
}

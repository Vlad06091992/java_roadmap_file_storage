package io.roadmap.filestorage.controller;

import io.minio.GetObjectResponse;
import io.roadmap.filestorage.components.PathResolver;
import io.roadmap.filestorage.dto.PathParams;
import io.roadmap.filestorage.dto.GetDirectoryDTO;
import io.roadmap.filestorage.dto.GetFileDTO;
import io.roadmap.filestorage.dto.interfaces.GetResourceData;
import io.roadmap.filestorage.services.DirectoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;


@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class ResourceController {

    private final DirectoryService directoryService;
    private final PathResolver pathResolver;

    @PostMapping("/resource")
    public ResponseEntity<Object> createDirectory(HttpServletRequest request, HttpServletResponse response) throws Exception {

        return new ResponseEntity<>("hz", HttpStatus.OK);
    }

    @GetMapping("/path-resolver")
    public Object getPathData(@Valid @ModelAttribute PathParams params) {
        String path = params.path();
        return pathResolver.getPathData(path);
    }

    @DeleteMapping("/resource")
    public ResponseEntity<Void> remove(@RequestParam String path) {
        directoryService.remove(path);
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

        GetObjectResponse result = directoryService.getData(path);

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

//    @GetMapping("/resource")
//    public ResponseEntity<GetResourceData> getQuery(@RequestParam("path") String path, InputStream inputStream) {
//
//        //dir = folder1/folder2/folder3/
//        //file = folder1/folder2/text.txt
//
//        Boolean isDirectory = (path.charAt(path.length() - 1)) == '/';
//
//        String[] parts = path.split("/");
//        String dirName =  parts[parts.length - 1] ;
//        String dirPath = parts.length > 1 ? path.substring(0, path.length() - 1) : "/";
//
//        String directoryName = parts[parts.length - 1];
//
//        int lastSlash = path.lastIndexOf('/');
//        String directoryPath = (parts.length > 1 ? path.substring(0, lastSlash) : "");
//
//        GetObjectResponse result = directoryService.getData(path);
//
//        Headers headers = result.headers();
//        String object = result.object();
////        log.info("headers: {}",headers);
////        log.info("object: {}",object);
//
//
//        String size = headers.get("Content-Length");
////        log.info("size: {}",size);
//
//        long s = Long.valueOf(size);
//
//        String type = isDirectory ? "DIRECTORY" : "FILE"
//                ;
//        GetResourceData getDirectoryDTO = isDirectory ? new GetDirectoryDTO(dirPath, dirName) : new GetFileDTO(dirPath, dirName,s);
//
//        log.info("Type: {}", getDirectoryDTO.type());
//
//        return new ResponseEntity<>(getDirectoryDTO, HttpStatus.CREATED);
//    }
}

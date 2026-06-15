package io.roadmap.filestorage.controller;

import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import io.roadmap.filestorage.dto.CreateBucketDTO;
import io.roadmap.filestorage.dto.CreateBucketResponseDTO;
import io.roadmap.filestorage.dto.LoginDTO;
import io.roadmap.filestorage.dto.RegisterOrLoginResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


@RestController
@RequestMapping("/api/minio")
@RequiredArgsConstructor
public class MinioController {
    private final MinioClient minioClient;

    @PostMapping("/create")
    public ResponseEntity<CreateBucketResponseDTO>createBucket (@Valid @RequestBody CreateBucketDTO createBucketDTO, HttpServletRequest request, HttpServletResponse response) throws MinioException {
        minioClient.makeBucket(
                MakeBucketArgs
                        .builder()
                        .bucket(createBucketDTO.bucketName())
                        .build());

        return new ResponseEntity<>(new CreateBucketResponseDTO(createBucketDTO.bucketName()), HttpStatus.CREATED);

    }

    @PostMapping("/upload")
    public ResponseEntity<Void> uploadFile() throws MinioException, FileNotFoundException {
        try (InputStream inputStream = getClass().getResourceAsStream("/Полис.pdf")) {

            if (inputStream == null) {
                throw new RuntimeException("Файл не найден в ресурсах");
            }

            minioClient.putObject(PutObjectArgs
                    .builder()
                    .bucket("user1111")
                    .object("Полис.pdf")
                .stream(inputStream, (long) inputStream.available(), (long) -1)
                    .build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}

package io.roadmap.filestorage.services;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.roadmap.filestorage.dto.GetDirectoryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectoryService {

    private final MinioClient minioClient;

    public Object splitPath(String path) {
        String[] parts = path.split("/");

        log.info("lengths: {}", parts.length);
        log.info("parts: {}", Arrays.toString(parts));

        String directoryName = parts[parts.length - 1];

        int lastSlash = path.lastIndexOf('/');
        String directoryPath = path.substring(0, lastSlash);

        log.info("directoryPath: {}, directoryName: {}", directoryPath, directoryName);

        GetDirectoryDTO getDirectoryDTO = new GetDirectoryDTO(directoryPath, directoryName);
        return getDirectoryDTO;
    }

    public GetDirectoryDTO createFolder(String path) throws Exception {

        String[] parts = path.split("/");
        String directoryName = parts[parts.length - 1];

        int lastSlash = path.lastIndexOf('/');
        String directoryPath = (parts.length > 1 ? path.substring(0, lastSlash) : "") + "/";


        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket("user1111")
                        .object(path + '/')
                        .stream(new ByteArrayInputStream(new byte[]{}), 0L, (long) -1)
                        .build()
        );

        return new GetDirectoryDTO(directoryPath, directoryName);
    }
}

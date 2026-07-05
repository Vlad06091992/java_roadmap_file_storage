package io.roadmap.filestorage.services;

import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import io.roadmap.filestorage.dto.GetDirectoryDTO;
import io.roadmap.filestorage.dto.GetFileDTO;
import io.roadmap.filestorage.exceptions.DirectoryAlreadyExistException;
import io.roadmap.filestorage.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectoryService {

    private List<Item> getList(Iterable<Result<Item>> iterable) {
        List<Item> list = new ArrayList<>();
        iterable.forEach(i -> {
            try {
                list.add(i.get());
            } catch (MinioException e) {
                throw new RuntimeException(e);
            }
        });
        return list;
    }

    private final MinioClient minioClient;


    public GetFileDTO saveFile(String path, MultipartFile file) throws Exception {

        String[] parts = path.split("/");
        String directoryName = parts[parts.length - 1];

        int lastSlash = path.lastIndexOf('/');
        String directoryPath = (parts.length > 1 ? path.substring(0, lastSlash) : "") + "/";

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket("user1111")
                            .object(path + "/" + file.getOriginalFilename()) // используем getOriginalFilename()
                            .stream(inputStream, file.getSize(), Long.valueOf(-1)) // передаем размер
                            .build()
            );
        }

        return new GetFileDTO(directoryPath, file.getOriginalFilename(), file.getSize());
    }


//    public Object splitPath(String path) {
//        String[] parts = path.split("/");
//
//        log.info("lengths: {}", parts.length);
//        log.info("parts: {}", Arrays.toString(parts));
//
//        String directoryName = parts[parts.length - 1];
//
//        int lastSlash = path.lastIndexOf('/');
//        String directoryPath = path.substring(0, lastSlash);
//
//        log.info("directoryPath: {}, directoryName: {}", directoryPath, directoryName);
//
//        GetDirectoryDTO getDirectoryDTO = new GetDirectoryDTO(directoryPath, directoryName);
//        return getDirectoryDTO;
//    }

    public GetDirectoryDTO createFolder(String path) throws Exception {

        String[] parts = path.split("/");
        String directoryName = parts[parts.length - 1];

        int lastSlash = path.lastIndexOf('/');
        String directoryPath = (parts.length > 1 ? path.substring(0, lastSlash) : "") + "/";



        boolean isExist = isExistObject(path);

        if(isExist){
            throw new DirectoryAlreadyExistException();
        }


        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket("user1111")
                        .object(path + '/')
                        .stream(new ByteArrayInputStream(new byte[]{}), 0L, (long) -1)
                        .build()
        );

        return new GetDirectoryDTO(directoryPath, directoryName);
    }

    // TODO вот это нужно переписать на minioClient.statObject() чтобы не тащить файл в ответ
    // сейчас мы закрывается объект GetObjectResponse stream, костыль
    public GetObjectResponse getData(String path) {
        try (GetObjectResponse stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket("user1111")
                        .object(path)
                        .build())) {
            // Read content


            return stream;
        } catch (MinioException e) {
            throw new ResourceNotFoundException();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public GetObjectResponse getObject(String path) {
        try  {
            GetObjectResponse stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket("user1111")
                            .object(path)
                            .build());

            return stream;
        } catch (MinioException e) {
            throw new ResourceNotFoundException();
        }
    }



    public List<Item> getFolderData(String path) {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket("user1111")
                        .prefix(path)
                        .recursive(false)
                        .build()


        );

        return getList(results);


    }

    public boolean isExistObject(String path){
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket("user1111")
                            .object(path)
                            .build()
            );

            return true;

        } catch (MinioException e) {
            return false;
        }
    }

    public void remove(String path) {
        try {
            // Проверяем существование объекта

            boolean isExist = isExistObject(path);

            if(!isExist){
                throw new ResourceNotFoundException();
            }

            // Если дошли сюда - объект существует
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket("user1111")
                            .object(path)
                            .build()
            );

        } catch (MinioException e) {
            System.err.println("Error occurred: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

}

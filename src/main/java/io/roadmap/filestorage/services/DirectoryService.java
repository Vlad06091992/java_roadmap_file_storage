package io.roadmap.filestorage.services;

import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.DeleteRequest;
import io.minio.messages.DeleteResult;
import io.minio.messages.Item;
import io.roadmap.filestorage.dto.GetDirectoryDTO;
import io.roadmap.filestorage.dto.GetFileDTO;
import io.roadmap.filestorage.exceptions.DirectoryAlreadyExistException;
import io.roadmap.filestorage.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    public void saveFromStream(InputStream stream, String path, String name, Long size) throws Exception {
        try {
            String name1 = path + "/" + name;
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket("user1111")
                            .object(path) // используем getOriginalFilename()
                            .stream(stream, size, Long.valueOf(-1)) // передаем размер
                            .build()
            );
        } catch (Exception e) {

        }

//        return new GetFileDTO(directoryPath, file.getOriginalFilename(), file.getSize());
    }


    public void saveFile(String path, MultipartFile[] files) throws Exception {

        String[] parts = path.split("/");
        String directoryName = parts[parts.length - 1];

        int lastSlash = path.lastIndexOf('/');
        String directoryPath = (parts.length > 1 ? path.substring(0, lastSlash) : "") + "/";


        for(MultipartFile file:files){
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket("user1111")
                                .object(path + "/" + file.getOriginalFilename()) // используем getOriginalFilename()
                                .stream(inputStream, file.getSize(), Long.valueOf(-1)) // передаем размер
                                .build()
                );
            }
        }



//        return new GetFileDTO(directoryPath, files[0].getOriginalFilename(), files[0].getSize());
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

        if (isExist) {
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

    public ByteArrayOutputStream downloadFolderAsZip(String path) throws Exception {
        // 1. Создаём поток для ZIP-архива в памяти
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        // 2. Получаем список всех объектов в папке (рекурсивно)
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket("user1111")
                        .prefix(path)  // например, "my-folder/"
                        .recursive(true)       // заходить во вложенные папки
                        .build()
        );

        // 3. Проходим по каждому файлу
        for (Result<Item> result : results) {
            Item item = result.get();
            String objectName = item.objectName();

            // Пропускаем, если это сама "папка" (в MinIO папки — это просто объекты с / в конце)
            if (objectName.endsWith("/")) {
                continue;
            }

            // Скачиваем содержимое файла
            try (InputStream is = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket("user1111")
                            .object(objectName)
                            .build())
            ) {
                // Добавляем файл в ZIP
                // Убираем префикс, чтобы внутри архива путь был относительным
                String entryName = objectName.replaceFirst("^" + path, "");
                zos.putNextEntry(new ZipEntry(entryName));

                // Копируем данные из MinIO в ZIP
                byte[] buffer = new byte[8192];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }

                zos.closeEntry();
            }
        }

        zos.close();
        return baos; // возвращаем ZIP как массив байтов
    }


    public GetObjectResponse getObject(String path) {

        boolean isFolder = path.endsWith("/");

            try {
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


    public InputStream getDownloadData(String path) throws Exception {

        boolean isFolder = path.endsWith("/");

        if(isFolder){
            ByteArrayOutputStream res = downloadFolderAsZip(path);
            return new ByteArrayInputStream(res.toByteArray());

        } else {
            try {
                return minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket("user1111")
                                .object(path)
                                .build());
            } catch (MinioException e) {
                throw new ResourceNotFoundException();
            }
        }
    }

    public boolean isExistObject(String path) {

        String prefix = path.endsWith("/") ? path : path + "/";
        boolean isFolder = path.endsWith("/");


        if (isFolder) {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket("user1111")
                            .prefix(prefix)
                            .maxKeys(1) // Optimization: Only look for the first matching item
                            .build()
            );

            boolean b = results.iterator().hasNext();
            return b;

        } else {
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
    }


    public void remove(String path) {
        try {
            boolean isFolder = path.endsWith("/");
            boolean isExist = isExistObject(path);

            if (!isExist) {
                throw new ResourceNotFoundException();
            }

            if (isFolder) {
                // Удаляем все объекты с префиксом (включая вложенные)
                Iterable<Result<Item>> results = minioClient.listObjects(
                        ListObjectsArgs.builder()
                                .bucket("user1111")
                                .prefix(path)
                                .recursive(true)
                                .build()
                );

                List<DeleteRequest.Object> objectsToDelete = new ArrayList<>();
                for (Result<Item> result : results) {
                    Item item = result.get();
                    objectsToDelete.add(new DeleteRequest.Object(item.objectName()));
                }

                if (objectsToDelete.isEmpty()) {
                    throw new ResourceNotFoundException(); // Папка пуста или не существует
                }

                Iterable<Result<DeleteResult.Error>> ress =  minioClient.removeObjects(
                        RemoveObjectsArgs.builder()
                                .bucket("user1111")
                                .objects(objectsToDelete)
                                .build()
                );

                for (Result<DeleteResult.Error> result : ress) {
                    DeleteResult.Error error = result.get();
                    System.out.println("Error in deleting object " + error.objectName() + "; " + error.message());
                }


            } else {
                // Удаляем файл
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket("user1111")
                                .object(path)
                                .build()
                );
            }

        } catch (MinioException e) {
            System.err.println("Error occurred: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

}

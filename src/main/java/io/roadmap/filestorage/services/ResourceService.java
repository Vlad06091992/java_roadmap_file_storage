package io.roadmap.filestorage.services;

import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.DeleteRequest;
import io.minio.messages.DeleteResult;
import io.minio.messages.Item;
import io.roadmap.filestorage.components.PathResolver;
import io.roadmap.filestorage.dtos.GetDirectoryDTO;
import io.roadmap.filestorage.dtos.GetFileDTO;
import io.roadmap.filestorage.dtos.interfaces.GetResourceData;
import io.roadmap.filestorage.exceptions.DirectoryAlreadyExistException;
import io.roadmap.filestorage.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceService {
    private final AuthService authService;
    private final MinioClient minioClient;
    private final PathResolver pathResolver;

    private List<Item> iterableToList(Iterable<Result<Item>> iterable) {
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

    private String getUserBucketName() {
        String name = authService
                .getCurrentUser()
                .getBucket()
                .getName();
        return name;
    }

    public void createBucket(String bucketName) {
        try {
            minioClient.makeBucket(
                    MakeBucketArgs
                            .builder()
                            .bucket(bucketName)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("bucket not created");
        }
    }

    public void saveFile(String path, MultipartFile[] files) throws Exception {

        String[] parts = path.split("/");
        String directoryName = parts[parts.length - 1];

        int lastSlash = path.lastIndexOf('/');
        String directoryPath = (parts.length > 1 ? path.substring(0, lastSlash) : "") + "/";


        for (MultipartFile file : files) {
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(getUserBucketName())
                                .object(path + "/" + file.getOriginalFilename()) // используем getOriginalFilename()
                                .stream(inputStream, file.getSize(), Long.valueOf(-1)) // передаем размер
                                .build()
                );
            }
        }
    }

    public GetDirectoryDTO createFolder(String path) throws Exception {
        PathResolver.PathData pathData = pathResolver.getPathData(path);
        String resourcePath = pathData.resourcePath();
        String resourceName = pathData.resourceName();
        boolean isExist = isExistResource(path);

        if (isExist) {
            throw new DirectoryAlreadyExistException();
        }

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(getUserBucketName())
                        .object(path + '/')
                        .stream(new ByteArrayInputStream(new byte[]{}), 0L, (long) -1)
                        .build()
        );

        return new GetDirectoryDTO(resourcePath, resourceName);
    }

    public StatObjectResponse getResourceData(String path) {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(getUserBucketName())
                            .object(path)
                            .build());

        } catch (MinioException e) {
            throw new ResourceNotFoundException();
        }
    }

    public ByteArrayOutputStream downloadFolderAsZip(String path) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(getUserBucketName())
                        .prefix(path)
                        .recursive(true)
                        .build()
        );

        for (Result<Item> result : results) {
            Item item = result.get();
            String objectName = item.objectName();

            if(pathResolver.isDirectory(objectName)){
                continue;
            }

            try (InputStream is = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(getUserBucketName())
                            .object(objectName)
                            .build())
            ) {
                String entryName = objectName.replaceFirst("^" + path, "");
                zos.putNextEntry(new ZipEntry(entryName));

                byte[] buffer = new byte[8192];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }

                zos.closeEntry();
            }
        }

        zos.close();
        return baos;
    }

    public List<Item> getFolderData(String path) {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(getUserBucketName())
                        .prefix(path)
                        .recursive(false)
                        .build()
        );
        return iterableToList(results);
    }

    private void moveFolder(String from, String to) throws Exception {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(getUserBucketName())
                        .prefix(from)
                        .recursive(true)
                        .build()
        );

        for (Result<Item> result : results) {
            Item item = result.get();
            String sourcePath = item.objectName();
            String relativePath = sourcePath.substring(from.length());
            String destPath = to + relativePath;

            moveFile(sourcePath,destPath);
        }
    }

    private void moveFile(String from, String to) throws Exception {
        minioClient.copyObject(
                CopyObjectArgs.builder()
                        .bucket(getUserBucketName())
                        .object(to)
                        .source(SourceObject.builder()
                                .bucket(getUserBucketName())
                                .object(from)
                                .build())
                        .build()
        );
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(getUserBucketName())
                        .object(from)
                        .build()
        );
    }

    public void move(String from, String to)  {
        try {
            boolean isFolder = from.endsWith("/");
            if (isFolder) {
                moveFolder(from, to);
            } else {
                moveFile(from, to);
            }
        } catch (Exception e){
            throw new RuntimeException("some");

        }
    }


    public InputStream getDownloadData(String path) throws Exception {
        if (pathResolver.isDirectory(path)) {
            ByteArrayOutputStream res = downloadFolderAsZip(path);
            return new ByteArrayInputStream(res.toByteArray());

        } else {
            try {
                return minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(getUserBucketName())
                                .object(path)
                                .build());
            } catch (MinioException e) {
                throw new ResourceNotFoundException();
            }
        }
    }

    public boolean isExistResource(String path) {
        if (pathResolver.isDirectory(path)) {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(getUserBucketName())
                            .prefix(path)
                            .maxKeys(1)
                            .build()
            );

            return results.iterator().hasNext();
        } else {
            try {
                minioClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(getUserBucketName())
                                .object(path)
                                .build()
                );
                return true;
            } catch (MinioException e) {
                return false;
            } catch (Exception e){
                throw new RuntimeException(e);
            }
        }
    }


    public void remove(String path) {
        try {
            boolean isExist = isExistResource(path);

            if (!isExist) {
                throw new ResourceNotFoundException();
            }

            if (pathResolver.isDirectory(path)) {
                Iterable<Result<Item>> results = minioClient.listObjects(
                        ListObjectsArgs.builder()
                                .bucket(getUserBucketName())
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
                    throw new ResourceNotFoundException();
                }

                Iterable<Result<DeleteResult.Error>> ress = minioClient.removeObjects(
                        RemoveObjectsArgs.builder()
                                .bucket(getUserBucketName())
                                .objects(objectsToDelete)
                                .build()
                );

                for (Result<DeleteResult.Error> result : ress) {
                    DeleteResult.Error error = result.get();
                    System.out.println("Error in deleting object " + error.objectName() + "; " + error.message());
                }


            } else {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(getUserBucketName())
                                .object(path)
                                .build()
                );
            }

        } catch (MinioException e) {
            System.err.println("Error occurred: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public GetResourceData generateResponseData(String path){
        PathResolver.PathData pathData = pathResolver.getPathData(path);
        Boolean isDirectory = pathData.isDirectory();
        String resourceName = pathData.resourceName();
        String resourcePath = pathData.resourcePath();
        StatObjectResponse result = getResourceData(path);
        Headers headers = result.headers();
        long sizeValue = Long.valueOf(headers.get("Content-Length"));

        return isDirectory ? new GetDirectoryDTO(resourcePath, resourceName) : new GetFileDTO(resourcePath, resourceName, sizeValue);
    }
}

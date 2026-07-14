package io.roadmap.filestorage.dtos;

import io.roadmap.filestorage.dtos.interfaces.GetResourceData;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Файл")
public record GetFileDTO(
        @Schema(description = "Путь к файлу относительно корня пользователя", example = "docs/")
        String path,
        @Schema(description = "Имя файла", example = "report.pdf")
        String name,
        @Schema(description = "Размер файла в байтах", example = "10240")
        Long size
) implements GetResourceData {


    public static GetFileDTO fromFullPath(String fullPath, Long size) {
        int lastSlash = fullPath.lastIndexOf('/');
        String path = fullPath.substring(0, lastSlash + 1);
        String name = fullPath.substring(lastSlash + 1);

        return new GetFileDTO(path, name, size);
    }

    @Override
    public ResourceTypes type() {
        return ResourceTypes.FILE;
    }
}

package io.roadmap.filestorage.dtos;


import io.roadmap.filestorage.dtos.interfaces.GetResourceData;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Папка")
public record GetDirectoryDTO(
        @Schema(description = "Путь к папке относительно корня пользователя", example = "docs/")
        String path,
        @Schema(description = "Имя папки (оканчивается на `/`)", example = "reports/")
        String name
) implements GetResourceData {

    public static GetDirectoryDTO fromFullPath(String fullPath) {
        String normalized = fullPath.endsWith("/")
                ? fullPath.substring(0, fullPath.length() - 1)
                : fullPath;

        int lastSlash = normalized.lastIndexOf('/');
        String path = normalized.substring(0, lastSlash + 1);
        String name = normalized.substring(lastSlash + 1);

        return new GetDirectoryDTO(path, name + "/");
    }

    @Override
    public ResourceTypes type() {
        return ResourceTypes.DIRECTORY;
    }
}

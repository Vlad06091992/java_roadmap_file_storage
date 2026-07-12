package io.roadmap.filestorage.dtos;


import io.roadmap.filestorage.dtos.interfaces.GetResourceData;

public record GetDirectoryDTO(
        String path,
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

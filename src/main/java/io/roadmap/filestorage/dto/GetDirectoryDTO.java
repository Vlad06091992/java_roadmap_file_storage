package io.roadmap.filestorage.dto;


import io.roadmap.filestorage.dto.interfaces.GetResourceData;

public record GetDirectoryDTO(
        String path,
        String name
) implements GetResourceData {

    public static GetDirectoryDTO fromFullPath(String fullPath) {

        //TODO нужна ли здесь подобная логика?

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

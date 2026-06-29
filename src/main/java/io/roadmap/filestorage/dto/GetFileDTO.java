package io.roadmap.filestorage.dto;

import io.roadmap.filestorage.dto.interfaces.GetResourceData;

public record GetFileDTO(
        String path,
        String name,
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

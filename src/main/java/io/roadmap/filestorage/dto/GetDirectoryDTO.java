package io.roadmap.filestorage.dto;


import io.roadmap.filestorage.dto.interfaces.GetResourceData;

public record GetDirectoryDTO(
        String path,
        String name
) implements GetResourceData {
    @Override
    public ResourceTypes type() {
        return ResourceTypes.DIRECTORY;
    }
}
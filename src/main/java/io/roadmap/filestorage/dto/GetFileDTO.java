package io.roadmap.filestorage.dto;

import io.roadmap.filestorage.dto.interfaces.GetResourceData;
import io.roadmap.filestorage.dto.interfaces.GetResourceSize;

public record GetFileDTO(
        String path,
        String name,
        long size
) implements GetResourceData, GetResourceSize {
    @Override
    public ResourceTypes type() {
        return ResourceTypes.FILE;
    }
}

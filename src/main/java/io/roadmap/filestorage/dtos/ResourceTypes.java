package io.roadmap.filestorage.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Тип ресурса", example = "FILE")
public enum ResourceTypes {
    DIRECTORY,
    FILE,
}

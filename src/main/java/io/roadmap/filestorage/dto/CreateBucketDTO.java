package io.roadmap.filestorage.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateBucketDTO(
        @NotNull
        @Size(min = 6, max = 30,
                message = "bucketName must be between 6 and 30 characters long")
        String bucketName) {
}
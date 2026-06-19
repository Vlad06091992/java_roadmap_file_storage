package io.roadmap.filestorage.dto;

public record GetDirectoryDTO(
        String path,
        String name,
        String type
) {
    public GetDirectoryDTO(String path, String name) {
        this(path, name, "DIRECTORY");
    }
}
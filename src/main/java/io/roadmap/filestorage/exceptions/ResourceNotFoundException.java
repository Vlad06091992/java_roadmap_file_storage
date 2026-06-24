package io.roadmap.filestorage.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException() {
        super("resource not found");
    }
}

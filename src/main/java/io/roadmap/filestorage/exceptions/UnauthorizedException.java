package io.roadmap.filestorage.exceptions;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException() {
        super("User not authenticated");
    }
}
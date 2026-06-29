package io.roadmap.filestorage.exceptions;

public class DirectoryAlreadyExistException extends RuntimeException {
    public DirectoryAlreadyExistException() {
        super("directory already exist");
    }
}

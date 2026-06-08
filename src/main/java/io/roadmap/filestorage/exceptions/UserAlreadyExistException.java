package io.roadmap.filestorage.exceptions;

public class UserAlreadyExistException extends RuntimeException {
    public UserAlreadyExistException() {
        super("user already exist");
    }
}

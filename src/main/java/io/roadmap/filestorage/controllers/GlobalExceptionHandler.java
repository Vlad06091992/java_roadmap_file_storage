package io.roadmap.filestorage.controllers;

import io.roadmap.filestorage.exceptions.DirectoryAlreadyExistException;
import io.roadmap.filestorage.exceptions.ResourceNotFoundException;
import io.roadmap.filestorage.exceptions.UnauthorizedException;
import io.roadmap.filestorage.exceptions.UserAlreadyExistException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private Map<String,String> getResponse(String message){
        Map<String, String> response = Map.of("message",message);
        return response;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleGlobalException(MethodArgumentNotValidException ex) {
        List<FieldError> errors = ex.getFieldErrors();
        Map<String, String> response = errors.stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing
                ));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<Object> handleGlobalException(UserAlreadyExistException ex) {
        return new ResponseEntity<>(getResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleGlobalException(ResourceNotFoundException ex) {
        return new ResponseEntity<>(getResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleGlobalException(RuntimeException ex) {
        return new ResponseEntity<>(getResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(DirectoryAlreadyExistException.class)
    public ResponseEntity<Object> handleGlobalException(DirectoryAlreadyExistException ex) {
        return new ResponseEntity<>(getResponse(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Object> handleGlobalException(UnauthorizedException ex) {
        return new ResponseEntity<>(getResponse(ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }
}

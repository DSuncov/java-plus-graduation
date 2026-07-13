package ru.practicum.exception;

public class FeignHandleException extends RuntimeException {
    public FeignHandleException(String message) {
        super(message);
    }
}

package net.guides.springboot2.springboot2swagger2.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}

package org.example.exception;

public class ResumeNotFoundException extends RuntimeException {
    public ResumeNotFoundException(String message) {
        super(message);
    }
}
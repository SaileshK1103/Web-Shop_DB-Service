package com.webshop.exception;

// Custom conflict exception used for throwing when there is conflict
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}

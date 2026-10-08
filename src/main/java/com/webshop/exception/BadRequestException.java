package com.webshop.exception;

// Custom bad request exception used when a request is well-formed but refers to something invalid
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}

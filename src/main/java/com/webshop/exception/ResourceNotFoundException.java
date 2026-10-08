package com.webshop.exception;

// custom not found exception
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Object id) {
        super(resourceName + " with id " + id + " was not found");
    }
}

package com.dsi.storage.exception;

/**
 * Custom checked exception class for storage-related errors.
 */
public class StorageException extends Exception {

    /**
     * Constructs a new StorageException with the specified detail message.
     * @param message The detail message.
     */
    public StorageException(String message) {
        super(message);
    }

    /**
     * Constructs a new StorageException with the specified detail message and cause.
     * @param message The detail message.
     * @param cause The cause of the exception.
     */
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
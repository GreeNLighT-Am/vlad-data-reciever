package ru.vlad.vlad_data_receiver.exceptions;

public class StorageException extends RuntimeException {
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
package ru.vlad.vlad_data_receiver.exceptions;

public class FileSavingException extends RuntimeException {
    public FileSavingException(String message, Throwable cause) {
        super(message, cause);
    }
}
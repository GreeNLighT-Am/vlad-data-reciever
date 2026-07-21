package ru.vlad.vlad_data_receiver.exceptions;

public class UnloadingNotFoundException extends RuntimeException {
    public UnloadingNotFoundException(String message) {
        super(message);
    }
}

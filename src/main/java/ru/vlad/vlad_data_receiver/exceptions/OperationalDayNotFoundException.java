package ru.vlad.vlad_data_receiver.exceptions;

public class OperationalDayNotFoundException extends RuntimeException {
    public OperationalDayNotFoundException(String message) {
        super(message);
    }
}
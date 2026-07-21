package ru.vlad.vlad_data_receiver.controller.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import ru.vlad.vlad_data_receiver.exceptions.BundleProcessingException;
import ru.vlad.vlad_data_receiver.exceptions.FileSavingException;
import ru.vlad.vlad_data_receiver.exceptions.OperationalDayNotFoundException;
import ru.vlad.vlad_data_receiver.exceptions.UnloadingNotFoundException;
import ru.vlad.vlad_data_receiver.exceptions.ValidationException;
import ru.vlad.vlad_data_receiver.exceptions.XmlParsingException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String ERROR_MESSAGE = "ErrorUnloadingEvent";

    @ExceptionHandler(XmlParsingException.class)
    public ResponseEntity<String> handleXmlParsingException(XmlParsingException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ERROR_MESSAGE);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<String> handleValidationException(ValidationException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ERROR_MESSAGE);
    }

    @ExceptionHandler(UnloadingNotFoundException.class)
    public ResponseEntity<String> handleUnloadingException(UnloadingNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ERROR_MESSAGE);
    }

    @ExceptionHandler(OperationalDayNotFoundException.class)
    public ResponseEntity<String> handleFindingOperationalDayException(OperationalDayNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ERROR_MESSAGE);
    }

    @ExceptionHandler(BundleProcessingException.class)
    public ResponseEntity<String> handleBundleProcessingException(BundleProcessingException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ERROR_MESSAGE);
    }

    @ExceptionHandler(FileSavingException.class)
    public ResponseEntity<String> handleFileSavingException(FileSavingException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ERROR_MESSAGE);
    }
}
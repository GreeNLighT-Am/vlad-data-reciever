package ru.vlad.vlad_data_receiver.exceptions;

public class XmlParsingException extends RuntimeException {
    public XmlParsingException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
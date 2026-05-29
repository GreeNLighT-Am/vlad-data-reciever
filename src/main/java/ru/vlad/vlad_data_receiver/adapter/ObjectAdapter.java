package ru.vlad.vlad_data_receiver.adapter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

public class ObjectAdapter extends XmlAdapter<String, Object> {

    private static final String DATE_TIME_PATTERN = "dd.MM.yyyy[ HH:mm:ss]";

    @Override
    public Object unmarshal(String xml) {
        return parseVariable(xml);
    }

    @Override
    public String marshal(Object object) {
        return object.toString();
    }

    private Object parseVariable(String variable) {
        Object obj;
        try {
            obj = ZonedDateTime.parse(variable).toLocalDateTime();
        } catch (DateTimeParseException e2) {
            try {
                DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                        .appendPattern(DATE_TIME_PATTERN)
                        .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                        .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                        .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                        .toFormatter();
                obj = LocalDateTime.parse(variable, formatter);
            } catch (DateTimeParseException e3) {
                obj = variable;
            }
        }

        return obj;
    }
}

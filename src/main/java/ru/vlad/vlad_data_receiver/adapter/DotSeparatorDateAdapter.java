package ru.vlad.vlad_data_receiver.adapter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class DotSeparatorDateAdapter extends XmlAdapter<String, LocalDateTime> {

    private static final String DATE_FORMAT = "dd.MM.yyyy";

    @Override
    public LocalDateTime unmarshal(String v) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern(DATE_FORMAT)
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .toFormatter();
        return LocalDateTime.parse(v, formatter);
    }

    @Override
    public String marshal(LocalDateTime v) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        return v.format(formatter);
    }
}

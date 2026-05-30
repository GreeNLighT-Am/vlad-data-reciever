package ru.vlad.vlad_data_receiver.parser.adapter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

    private static final String ZONED_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    @Override
    public LocalDateTime unmarshal(String xml) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(xml);
        return zonedDateTime.toLocalDateTime();
    }

    @Override
    public String marshal(LocalDateTime object) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(ZONED_DATE_TIME_PATTERN);
        return object.format(formatter);
    }
}
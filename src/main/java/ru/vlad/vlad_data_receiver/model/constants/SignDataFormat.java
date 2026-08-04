package ru.vlad.vlad_data_receiver.model.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SignDataFormat {
    XML("xml"),
    TXT("txt");

    private final String format;

    public static boolean isValid(String format) {
        for (SignDataFormat signDataFormat : values()) {
            if (signDataFormat.format.equalsIgnoreCase(format)) {
                return true;
            }
        }
        return false;
    }
}
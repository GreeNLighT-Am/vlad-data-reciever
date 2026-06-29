package ru.vlad.vlad_data_receiver.model.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SignDataFormats {
    XML("xml"),
    TXT("txt");

    private final String format;

    public static boolean isValid(String format) {
        for (SignDataFormats signDataFormats : values()) {
            if (signDataFormats.format.equalsIgnoreCase(format)) {
                return true;
            }
        }
        return false;
    }
}
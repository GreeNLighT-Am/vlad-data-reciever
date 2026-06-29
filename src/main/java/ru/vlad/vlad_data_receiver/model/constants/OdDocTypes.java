package ru.vlad.vlad_data_receiver.model.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OdDocTypes {
    KKD("ККД"),
    BDD("БДД"),
    FO("ФО");

    private final String value;

    public static boolean isValid(String type) {
        for (OdDocTypes odDocTypes : values()) {
            if (odDocTypes.value.equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }
}
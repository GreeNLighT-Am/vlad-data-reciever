package ru.vlad.vlad_data_receiver.model.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OdDocTypes {
    KKD("ККД", 1),
    BDD("БДД", 2),
    FO("ФО", 3);

    private final String value;
    private final int type;

    public static boolean isValid(String type) {
        for (OdDocTypes odDocTypes : values()) {
            if (odDocTypes.value.equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    public static int getType(String value) {
        for (OdDocTypes odDocTypes : values()) {
            if (odDocTypes.value.equals(value)) {
                return odDocTypes.getType();
            }
        }
        throw new IllegalArgumentException("Unknown odDocType: " + value);
    }
}
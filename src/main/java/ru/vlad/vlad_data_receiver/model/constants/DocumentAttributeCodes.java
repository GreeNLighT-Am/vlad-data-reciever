package ru.vlad.vlad_data_receiver.model.constants;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public enum DocumentAttributeCodes {
    DocType(true),
    DocNumber(true),
    DocDate(true),
    DocAccount(true),
    DocSourceSystem(true),
    DocTimeStamp(true),
    DocStatus(true),
    DocSum(false),
    DocSKOSymbol(false),
    DocSign1(false),
    DocSign2(false);

    private final boolean required;

    DocumentAttributeCodes(boolean required) {
        this.required = required;
    }

    public boolean isRequired() {
        return required;
    }

    private static final Map<String, DocumentAttributeCodes> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(DocumentAttributeCodes::name, e -> e));

    private static final Set<String> REQUIRED_CODES = Arrays.stream(values())
            .filter(DocumentAttributeCodes::isRequired)
            .map(DocumentAttributeCodes::name)
            .collect(Collectors.toUnmodifiableSet());

    public static DocumentAttributeCodes fromString(String code) {
        return BY_NAME.get(code);
    }

    public static Set<String> getRequiredCodes() {
        return REQUIRED_CODES;
    }
}
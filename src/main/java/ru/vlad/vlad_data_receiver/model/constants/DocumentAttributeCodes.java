package ru.vlad.vlad_data_receiver.model.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum DocumentAttributeCodes {
    DOC_TYPE("DocType", true),
    DOC_NUMBER("DocNumber", true),
    DOC_DATE("DocDate", true),
    DOC_ACCOUNT("DocAccount", true),
    DOC_SOURCE_SYSTEM("DocSourceSystem", true),
    DOC_TIME_STAMP("DocTimeStamp", true),
    DOC_STATUS("DocStatus", true),
    DOC_SUM("DocSum", false),
    DOC_SKO_SYMBOL("DocSKOSymbol", false),
    DOC_SIGN_1("DocSign1", false),
    DOC_SIGN_2("DocSign2", false),
    DOC_SIGN_3("DocSign3", false);


    private final String code;
    private final boolean required;

    public static DocumentAttributeCodes fromString(String code) {
        for (DocumentAttributeCodes documentAttributeCodes : values()) {
            if (documentAttributeCodes.getCode().equals(code)) {
                return documentAttributeCodes;
            }
        }
        return null;
    }

    public static Set<String> getRequiredCodes() {
        return Arrays.stream(values())
                .filter(DocumentAttributeCodes::isRequired)
                .map(DocumentAttributeCodes::getCode)
                .collect(Collectors.toUnmodifiableSet());
    }
}
package ru.vlad.vlad_data_receiver.model.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum DocumentAttributeCode {
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

    public static DocumentAttributeCode fromString(String code) {
        for (DocumentAttributeCode documentAttributeCode : values()) {
            if (documentAttributeCode.getCode().equals(code)) {
                return documentAttributeCode;
            }
        }
        return null;
    }

    public static Set<String> getRequiredCodes() {
        return Arrays.stream(values())
                .filter(DocumentAttributeCode::isRequired)
                .map(DocumentAttributeCode::getCode)
                .collect(Collectors.toUnmodifiableSet());
    }
}
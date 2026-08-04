package ru.vlad.vlad_data_receiver.util;

import lombok.experimental.UtilityClass;
import ru.vlad.vlad_data_receiver.model.constants.DocumentAttributeCode;
import ru.vlad.vlad_data_receiver.parser.documents.Document;
import ru.vlad.vlad_data_receiver.parser.documents.DocumentCard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@UtilityClass
public final class DocumentAttributeExtractor {
    public static String getAttributeValue(Document document, DocumentAttributeCode code) {
        return getAttributeValue(document.getDocumentCard(), code);
    }

    public static String getAttributeValue(DocumentCard card, DocumentAttributeCode code) {
        return card.getVariableAttribute().stream()
                .filter(attr -> code.getCode().equals(attr.getAttributeCode()))
                .findFirst()
                .map(attr -> attr.getAttributeValue().toString())
                .orElse(null);
    }

    public static Integer getAttributeAsInteger(Document document, DocumentAttributeCode code) {
        return getAttributeAsInteger(document.getDocumentCard(), code);
    }

    public static Integer getAttributeAsInteger(DocumentCard card, DocumentAttributeCode code) {
        return Integer.parseInt(getAttributeValue(card, code));
    }

    public static BigDecimal getAttributeAsBigDecimal(Document document, DocumentAttributeCode code) {
        String value = getAttributeValue(document, code);
        return value != null ? new BigDecimal(value) : null;
    }

    public static String getSourceSystemCode(DocumentCard card) {
        return getAttributeValue(card, DocumentAttributeCode.DOC_SOURCE_SYSTEM);
    }

    public static Integer getDepartmentNumber(DocumentCard card) {
        return getAttributeAsInteger(card, DocumentAttributeCode.DOC_ACCOUNT);
    }

    public static LocalDate getOperationalDayDate(DocumentCard card) {
        return LocalDateTime.parse(getAttributeValue(card, DocumentAttributeCode.DOC_DATE)).toLocalDate();
    }
}
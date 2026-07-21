package ru.vlad.vlad_data_receiver.util;

import ru.vlad.vlad_data_receiver.model.constants.DocumentAttributeCodes;
import ru.vlad.vlad_data_receiver.parser.documents.Document;
import ru.vlad.vlad_data_receiver.parser.documents.DocumentCard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class DocumentAttributeExtractor {
    private DocumentAttributeExtractor() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String getAttributeValue(Document document, DocumentAttributeCodes code) {
        return getAttributeValue(document.getDocumentCard(), code);
    }

    public static String getAttributeValue(DocumentCard card, DocumentAttributeCodes code) {
        return card.getVariableAttribute().stream()
                .filter(attr -> code.getCode().equals(attr.getAttributeCode()))
                .findFirst()
                .map(attr -> attr.getAttributeValue().toString())
                .orElse(null);
    }

    public static Integer getAttributeAsInteger(Document document, DocumentAttributeCodes code) {
        return getAttributeAsInteger(document.getDocumentCard(), code);
    }

    public static Integer getAttributeAsInteger(DocumentCard card, DocumentAttributeCodes code) {
        return Integer.parseInt(getAttributeValue(card, code));
    }

    public static BigDecimal getAttributeAsBigDecimal(Document document, DocumentAttributeCodes code) {
        String value = getAttributeValue(document, code);
        return value != null ? new BigDecimal(value) : null;
    }

    public static String getSourceSystemCode(DocumentCard card) {
        return getAttributeValue(card, DocumentAttributeCodes.DOC_SOURCE_SYSTEM);
    }

    public static Integer getDepartmentNumber(DocumentCard card) {
        return getAttributeAsInteger(card, DocumentAttributeCodes.DOC_ACCOUNT);
    }

    public static LocalDate getOperationalDayDate(DocumentCard card) {
        return LocalDateTime.parse(getAttributeValue(card, DocumentAttributeCodes.DOC_DATE)).toLocalDate();
    }
}
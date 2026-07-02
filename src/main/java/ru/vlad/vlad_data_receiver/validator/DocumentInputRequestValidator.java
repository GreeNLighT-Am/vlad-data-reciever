package ru.vlad.vlad_data_receiver.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.vlad.vlad_data_receiver.exceptions.ValidationException;
import ru.vlad.vlad_data_receiver.model.constants.DocumentAttributeCodes;
import ru.vlad.vlad_data_receiver.model.constants.ContentFormats;
import ru.vlad.vlad_data_receiver.model.constants.OdDocTypes;
import ru.vlad.vlad_data_receiver.model.constants.SignDataFormats;
import ru.vlad.vlad_data_receiver.parser.documents.Content;
import ru.vlad.vlad_data_receiver.parser.documents.Document;
import ru.vlad.vlad_data_receiver.parser.documents.DocumentAttribute;
import ru.vlad.vlad_data_receiver.parser.documents.DocumentBody;
import ru.vlad.vlad_data_receiver.parser.documents.DocumentCard;
import ru.vlad.vlad_data_receiver.parser.documents.DocumentInputRequest;
import ru.vlad.vlad_data_receiver.parser.documents.DocumentSigns;
import ru.vlad.vlad_data_receiver.parser.documents.Sign;
import ru.vlad.vlad_data_receiver.parser.documents.SignBody;
import ru.vlad.vlad_data_receiver.service.DepartmentsService;
import ru.vlad.vlad_data_receiver.service.DoctypesService;
import ru.vlad.vlad_data_receiver.service.SourceSystemsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentInputRequestValidator {
    private final DoctypesService doctypesService;
    private final DepartmentsService departmentsService;
    private final SourceSystemsService sourceSystemsService;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final Pattern FULL_NAME_PATTERN = Pattern.compile(
            "^[А-ЯЁ][а-яё]+(?:-[А-ЯЁ][а-яё]+)?\\s" +
                    "[А-ЯЁ][а-яё]+(?:-[А-ЯЁ][а-яё]+)?" +
                    "\\s[А-ЯЁ][а-яё]+(?:-[А-ЯЁ][а-яё]+)?$"
    );
    private static final String ERROR_MESSAGE = "ErrorUnloadingEvent";

    private void processValidationError(String message) {
        log.error(message);
        throw new ValidationException(ERROR_MESSAGE);
    }

    private void validateNotNull(Object object, String attributeCode) {
        if (object == null) {
            processValidationError(String.format("Атрибут %s не передан или передан некорректный формат", attributeCode));
        }
    }

    private void validateIsPositive(int value, String attributeCode) {
        if (value <= 0) {
            processValidationError(String.format("Атрибут %s не передан или передано некорректное значение", attributeCode));
        }
    }

    private void nullOrBlankAttributeValidation(String attributeValue, String attribute) {
        if (attributeValue == null) {
            processValidationError(String.format("Атрибут %s не передан", attribute));
        } else if (attributeValue.isBlank()) {
            processValidationError(String.format("В атрибут %s передано пустое значение", attribute));
        }
    }

    private void nullOrBlankWithSeqNFormatValidation(String format, String tag, String attribute, int seqN) {
        if (format == null) {
            processValidationError(String.format("В документе №%d в тэг %s не передан атрибут %s", seqN, tag, attribute));
        } else if (format.isBlank()) {
            processValidationError(String.format("В документе №%d в тэге %s в атрибут %s не передано значение", seqN, tag, attribute));
        }
    }

    private String nullOrBlankObjectValidationAndToString(Object object, int seqN, String attributeCode) {
        if (object == null) {
            processValidationError(String.format("В документе №%d не передано значение атрибута \"%s\"", seqN, attributeCode));
        }

        String objectStr = object.toString();
        if (objectStr.isBlank()) {
            processValidationError(String.format("В документе №%d передано пустое значение атрибута %s", seqN, attributeCode));
        }

        return objectStr;
    }

    public void validate(DocumentInputRequest documentInputRequest) {
        validateAttributes(documentInputRequest);
        validateDocuments(documentInputRequest.getDocument(), documentInputRequest.getTotalDocs());
    }

    private void validateAttributes(DocumentInputRequest documentInputRequest) {

        nullOrBlankAttributeValidation(documentInputRequest.getID(), "ID");

        validateNotNull(documentInputRequest.getTimeStamp(), "TimeStamp");

        validateIsPositive(documentInputRequest.getBlockNum(), "BlockNum");

        validateIsPositive(documentInputRequest.getTotalDocs(), "TotalDocs");

        nullOrBlankAttributeValidation(documentInputRequest.getDataSet(), "dataSet");

        validateOdDocType(documentInputRequest.getOdDocType());
    }

    private void validateOdDocType(String odDocType) {
        String attributeName = "odDocType";
        nullOrBlankAttributeValidation(odDocType, attributeName);

        if (!OdDocTypes.isValid(odDocType)) {
            processValidationError(String.format("В атрибут %s передано некорректное значение, допустимые: %s", attributeName));
        }
    }

    private void validateDocuments(List<Document> documents, int totalDocs) {
        if (documents.isEmpty()) {
            processValidationError("Не обнаружен ни один тэг Document)");
        }

        for (Document Document : documents) {
            validateDocument(Document, totalDocs);
        }
    }

    private void validateDocument(Document document, int totalDocs) {
        int seqN = document.getSeqN();

        if (totalDocs > 1 && seqN <= 0) {
            processValidationError("В одном или нескольких тэгах Document не передан атрибут SeqN или передано некорректное значение");

        } else if (totalDocs > 1 && seqN > totalDocs) {
            processValidationError("В одном или нескольких тэгах Document передан атрибут SeqN со значением превышающим значение атрибута totalDocs");
        } else if (totalDocs == 1 && seqN <= 0) {
            seqN = 1;
        }

        validateDocumentCard(document.getDocumentCard(), seqN);
        validateDocumentBody(document.getDocumentBody(), seqN);
    }

    private void validateDocumentCard(DocumentCard documentCard, int seqN) {
        if (documentCard == null) {
            processValidationError(String.format("В документе №%d не передан тэг DocumentCard", seqN));
        }

        validateVariableAttributes(documentCard.getVariableAttribute(), seqN);
    }


    private void validateVariableAttributes(List<DocumentAttribute> attributesList, int seqN) {
        if (attributesList.isEmpty()) {
            processValidationError(String.format("В документе №%d, в тэг DocumentCard не передан ни один тэг VariableAttribute", seqN));
        }

        Set<String> requiredAttributes = DocumentAttributeCodes.getRequiredCodes();
        Set<String> foundAttributes = new HashSet<>();

        for (DocumentAttribute attr : attributesList) {
            String attrCodeStr = attr.getAttributeCode();
            Object attrValue = attr.getAttributeValue();

            if (attrCodeStr == null) {
                processValidationError(String.format("В документе №%d в одном или нескольких тегах VariableAttribute не передан атрибут AttributeCode", seqN));
            } else if (attrCodeStr.isBlank()) {
                processValidationError(String.format("В документе №%d, в одном или нескольких тегах VariableAttribute в атрибут AttributeCode передано пустое значение", seqN));
            }

            foundAttributes.add(attrCodeStr);

            DocumentAttributeCodes attrCode = DocumentAttributeCodes.fromString(attrCodeStr);
            if (attrCode == null) {
                continue;
            }

            switch (attrCode) {
                case DOC_TYPE -> validateDocType(attrValue, seqN);
                case DOC_NUMBER ->
                        nullOrBlankObjectValidationAndToString(attrValue, seqN, DocumentAttributeCodes.DOC_NUMBER.getCode());
                case DOC_DATE ->
                        validateByDateTimeFormatter(attrValue, seqN, DocumentAttributeCodes.DOC_DATE.getCode(), DATE_FORMATTER);
                case DOC_ACCOUNT -> validateDocAccount(attrValue, seqN);
                case DOC_SOURCE_SYSTEM -> validateDocSourceSystem(attrValue, seqN);
                case DOC_TIME_STAMP ->
                        validateByDateTimeFormatter(attrValue, seqN, DocumentAttributeCodes.DOC_TIME_STAMP.getCode(), TIMESTAMP_FORMATTER);
                case DOC_STATUS -> validateDocStatus(attrValue, seqN);
                case DOC_SUM -> validateDocSum(attrValue, seqN);
                case DOC_SKO_SYMBOL -> validateDocSKOSymbol(attrValue, seqN);
                case DOC_SIGN_1 -> validateDocSigns(attrValue, seqN, DocumentAttributeCodes.DOC_SIGN_1.getCode());
                case DOC_SIGN_2 -> validateDocSigns(attrValue, seqN, DocumentAttributeCodes.DOC_SIGN_2.getCode());
            }
        }

        Set<String> missingAttributes = new HashSet<>(requiredAttributes);
        missingAttributes.removeAll(foundAttributes);

        if (!missingAttributes.isEmpty()) {
            processValidationError(String.format("В документе №%d не передан обязательный атрибут \"%s\" или передан недопустимый",
                    seqN, String.join(", ", missingAttributes)));
        }
    }

    private void validateDocType(Object docType, int seqN) {
        String attributeCode = DocumentAttributeCodes.DOC_TYPE.getCode();
        String docTypeSrt = nullOrBlankObjectValidationAndToString(docType, seqN, attributeCode);

        if (!doctypesService.isDoctypesValid(docTypeSrt)) {
            processValidationError(String.format("В документе №%d невалидное значение аттрибута %s", seqN, attributeCode));
        }
    }

    private void validateByDateTimeFormatter(Object object, int seqN, String attributeCode, DateTimeFormatter formatter) {
        String objStr = nullOrBlankObjectValidationAndToString(object, seqN, attributeCode);

        if (object instanceof LocalDateTime ldt) {
            objStr = ldt.format(formatter);
        }

        try {
            formatter.parse(objStr);
        } catch (DateTimeParseException e) {
            processValidationError(String.format("В документе №%d в атрибут %s передано некорректное значение", seqN, attributeCode));
        }
    }

    private void validateDocAccount(Object docAccount, int seqN) {
        String attributeCode = DocumentAttributeCodes.DOC_ACCOUNT.getCode();
        String docAccountStr = nullOrBlankObjectValidationAndToString(docAccount, seqN, attributeCode);

        try {
            int docAccountInt = Integer.parseInt(docAccountStr);
            if (!departmentsService.isDepartmentValid(docAccountInt)) {
                processValidationError(String.format("В документе №%d невалидное значение аттрибута %s", seqN, attributeCode));
            }
        } catch (NumberFormatException e) {
            processValidationError(String.format("В документе №%d некорректное значение аттрибута %s", seqN, attributeCode));
        }
    }

    private void validateDocSourceSystem(Object docSourceSystem, int seqN) {
        String attributeCode = DocumentAttributeCodes.DOC_SOURCE_SYSTEM.getCode();
        String docSourceSystemStr = nullOrBlankObjectValidationAndToString(docSourceSystem, seqN, attributeCode);

        if (!sourceSystemsService.isSourceSystemValid(docSourceSystemStr)) {
            processValidationError(String.format("В документе №%d невалидное значение аттрибута %s", seqN, attributeCode));
        }
    }

    private void validateDocSum(Object docSum, int seqN) {
        String attributeCode = DocumentAttributeCodes.DOC_SUM.getCode();
        String docSumStr = nullOrBlankObjectValidationAndToString(docSum, seqN, attributeCode);

        try {
            double docSumDouble = Double.parseDouble(docSumStr);
            if (docSumDouble < 0) {
                processValidationError(String.format("В документе №%d в атрибут %s передано отрицательное значение", seqN, attributeCode));
            }
        } catch (NumberFormatException e) {
            processValidationError(String.format("В документе №%d в атрибут %s передано невалидное значение", seqN, attributeCode));
        }
    }

    private void validateDocSKOSymbol(Object docSKOSymbol, int seqN) {
        String attributeCode = DocumentAttributeCodes.DOC_SKO_SYMBOL.getCode();
        String docSKOSymbolStr = nullOrBlankObjectValidationAndToString(docSKOSymbol, seqN, attributeCode);
        int docSKOSymbolInt = parsingInt(docSKOSymbolStr, seqN, attributeCode);

        if (docSKOSymbolInt < 0) {
            processValidationError(String.format("В документе №%d в атрибут %s передано отрицательное значение", seqN, attributeCode));
        }
    }

    private void validateDocStatus(Object docStatus, int seqN) {
        String attributeCode = DocumentAttributeCodes.DOC_STATUS.getCode();
        String docStatusStr = nullOrBlankObjectValidationAndToString(docStatus, seqN, attributeCode);
        int docStatusInt = parsingInt(docStatusStr, seqN, attributeCode);

        if (docStatusInt != 0 && docStatusInt != 1) {
            processValidationError(String.format("В документе №%d в атрибут %s передано некорректное значение, допустимы 0 или 1", seqN, attributeCode));
        }
    }

    private int parsingInt(String value, int seqN, String attributeCode) {
        int valueInt = 0;
        try {
            valueInt = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            processValidationError(String.format("В документе №%d в атрибут %s передано невалидное значение ", seqN, attributeCode));
        }
        return valueInt;
    }

    private void validateDocSigns(Object object, int seqN, String attributeCode) {
        String objectStr = nullOrBlankObjectValidationAndToString(object, seqN, attributeCode);

        if (!FULL_NAME_PATTERN.matcher(objectStr).matches()) {
            processValidationError(String.format("В документе №%d в атрибут %s передано невалидное значение", seqN, attributeCode));
        }
    }

    private void validateDocumentBody(DocumentBody documentBody, int seqN) {
        if (documentBody == null) {
            processValidationError(String.format("В документе №%d не передан тэг DocumentBody", seqN));
        }

        validateContent(documentBody.getContent(), seqN);
    }

    private void validateContent(List<Content> contentList, int seqN) {
        if (contentList.isEmpty()) {
            processValidationError(String.format("В документе №%d в тэг DocumentBody не передан тэг Content", seqN));
        }

        for (Content content : contentList) {
            validateContentFormat(content.getFormat(), seqN);
            validateDocumentSings(content.getDocumentSigns(), seqN);
        }
    }

    private void validateContentFormat(String contentFormat, int seqN) {
        String tag = "Content";
        String attribute = "Format";

        nullOrBlankWithSeqNFormatValidation(contentFormat, tag, attribute, seqN);

        if (!ContentFormats.isValid(contentFormat)) {
            processValidationError(String.format("В документе №%d в тэге %s в атрибут %s передано некорректное значение", seqN, tag, attribute));
        }
    }

    private void validateDocumentSings(DocumentSigns documentSigns, int seqN) {
        if (documentSigns == null) {
            processValidationError(String.format("В документе №%d в тэг Content не передан тэг DocumentSigns", seqN));
        }

        validateSign(documentSigns.getSign(), seqN);
    }

    private void validateSign(List<Sign> sign, int seqN) {
        if (sign.isEmpty()) {
            processValidationError(String.format("В документе №%d в тэг DocumentSigns не передан тэг Sign", seqN));
        }

        for (Sign signs : sign) {
            validateSignData(signs.getSignData(), seqN);
        }
    }

    private void validateSignData(SignBody signData, int seqN) {
        if (signData == null) {
            processValidationError(String.format("В документе №%d в тэг Sign не передан тэг SignData", seqN));
        }

        validateSignDataFormat(signData.getFormat(), seqN);
    }

    private void validateSignDataFormat(String signDataFormat, int seqN) {
        String tag = "SignData";
        String attribute = "Format";

        nullOrBlankWithSeqNFormatValidation(signDataFormat, tag, attribute, seqN);

        if (!SignDataFormats.isValid(signDataFormat)) {
            processValidationError(String.format("В документе №%d в тэге %s в атрибут %s передано невалидное значение", seqN, tag, attribute));
        }
    }
}
package ru.vlad.vlad_data_receiver.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.vlad.vlad_data_receiver.exceptions.ValidationException;
import ru.vlad.vlad_data_receiver.model.constants.ContentFormat;
import ru.vlad.vlad_data_receiver.model.constants.DocumentAttributeCode;
import ru.vlad.vlad_data_receiver.model.constants.OdDocType;
import ru.vlad.vlad_data_receiver.model.constants.SignDataFormat;
import ru.vlad.vlad_data_receiver.model.constants.UnloadingState;
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
import ru.vlad.vlad_data_receiver.service.UnloadingCrudService;

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
    private final UnloadingCrudService unloadingCrudService;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final Pattern FULL_NAME_PATTERN = Pattern.compile(
            "^[А-ЯЁ][а-яё]+(?:-[А-ЯЁ][а-яё]+)?\\s" +
                    "[А-ЯЁ][а-яё]+(?:-[А-ЯЁ][а-яё]+)?" +
                    "\\s[А-ЯЁ][а-яё]+(?:-[А-ЯЁ][а-яё]+)?$"
    );
    private static final String ERROR_MESSAGE = "Ошибка валидации";

    public void validate(DocumentInputRequest documentInputRequest) {
        String id = documentInputRequest.getID();

        nullOrBlankAttributeValidation(id, "ID", id);

        validateNotNull(documentInputRequest.getTimeStamp(), "TimeStamp", id);

        validateIsPositive(documentInputRequest.getBlockNum(), "BlockNum", id);

        validateIsPositive(documentInputRequest.getTotalDocs(), "TotalDocs", id);

        nullOrBlankAttributeValidation(documentInputRequest.getDataSet(), "dataSet", id);

        validateOdDocType(documentInputRequest.getOdDocType(), id);

        validateDocuments(documentInputRequest.getDocument(), documentInputRequest.getTotalDocs(), id);
    }

    private void validateOdDocType(String odDocType, String id) {
        String attributeName = "odDocType";
        nullOrBlankAttributeValidation(odDocType, attributeName, id);

        if (!OdDocType.isValid(odDocType)) {
            processValidationError(String.format("В атрибут %s передано некорректное значение", attributeName), id);
        }
    }

    private void validateDocuments(List<Document> documents, int totalDocs, String id) {
        if (documents.isEmpty()) {
            processValidationError("Не обнаружен ни один тэг Document)", id);
        }

        for (Document Document : documents) {
            validateDocument(Document, totalDocs, id);
        }
    }

    private void validateDocument(Document document, int totalDocs, String id) {
        int seqN = document.getSeqN();

        if (totalDocs > 1 && seqN <= 0) {
            processValidationError("В одном или нескольких тэгах Document не передан атрибут SeqN или передано некорректное значение", id);

        } else if (totalDocs > 1 && seqN > totalDocs) {
            processValidationError("В одном или нескольких тэгах Document передан атрибут SeqN со значением превышающим значение атрибута totalDocs", id);
        } else if (totalDocs == 1 && seqN <= 0) {
            seqN = 1;
        }

        validateDocumentCard(document.getDocumentCard(), seqN, id);
        validateDocumentBody(document.getDocumentBody(), seqN, id);
    }

    private void validateDocumentCard(DocumentCard documentCard, int seqN, String id) {
        if (documentCard == null) {
            processValidationError(String.format("В документе №%d не передан тэг DocumentCard", seqN), id);
        }

        validateVariableAttributes(documentCard.getVariableAttribute(), seqN, id);
    }


    private void validateVariableAttributes(List<DocumentAttribute> attributesList, int seqN, String id) {
        if (attributesList.isEmpty()) {
            processValidationError(String.format("В документе №%d, в тэг DocumentCard не передан ни один тэг VariableAttribute", seqN), id);
        }

        Set<String> requiredAttributes = DocumentAttributeCode.getRequiredCodes();
        Set<String> foundAttributes = new HashSet<>();

        for (DocumentAttribute attr : attributesList) {
            String attrCodeStr = attr.getAttributeCode();
            Object attrValue = attr.getAttributeValue();

            if (attrCodeStr == null) {
                processValidationError(String.format("В документе №%d в одном или нескольких тегах VariableAttribute не передан атрибут AttributeCode", seqN), id);
            } else if (attrCodeStr.isBlank()) {
                processValidationError(String.format("В документе №%d, в одном или нескольких тегах VariableAttribute в атрибут AttributeCode передано пустое значение", seqN), id);
            }

            foundAttributes.add(attrCodeStr);

            DocumentAttributeCode attrCode = DocumentAttributeCode.fromString(attrCodeStr);
            if (attrCode == null) {
                continue;
            }

            switch (attrCode) {
                case DOC_TYPE -> validateDocType(attrValue, seqN, id);
                case DOC_NUMBER ->
                        nullOrBlankObjectValidationAndToString(attrValue, seqN, DocumentAttributeCode.DOC_NUMBER.getCode(), id);
                case DOC_DATE ->
                        validateByDateTimeFormatter(attrValue, seqN, DocumentAttributeCode.DOC_DATE.getCode(), DATE_FORMATTER, id);
                case DOC_ACCOUNT -> validateDocAccount(attrValue, seqN, id);
                case DOC_SOURCE_SYSTEM -> validateDocSourceSystem(attrValue, seqN, id);
                case DOC_TIME_STAMP ->
                        validateByDateTimeFormatter(attrValue, seqN, DocumentAttributeCode.DOC_TIME_STAMP.getCode(), TIMESTAMP_FORMATTER, id);
                case DOC_STATUS -> validateDocStatus(attrValue, seqN, id);
                case DOC_SUM -> validateDocSum(attrValue, seqN, id);
                case DOC_SKO_SYMBOL -> validateDocSKOSymbol(attrValue, seqN, id);
                case DOC_SIGN_1 -> validateDocSigns(attrValue, seqN, DocumentAttributeCode.DOC_SIGN_1.getCode(), id);
                case DOC_SIGN_2 -> validateDocSigns(attrValue, seqN, DocumentAttributeCode.DOC_SIGN_2.getCode(), id);
            }
        }

        Set<String> missingAttributes = new HashSet<>(requiredAttributes);
        missingAttributes.removeAll(foundAttributes);

        if (!missingAttributes.isEmpty()) {
            processValidationError(String.format("В документе №%d не передан обязательный атрибут \"%s\" или передан недопустимый",
                    seqN, String.join(", ", missingAttributes)), id);
        }
    }

    private void validateDocType(Object docType, int seqN, String id) {
        String attributeCode = DocumentAttributeCode.DOC_TYPE.getCode();
        String docTypeSrt = nullOrBlankObjectValidationAndToString(docType, seqN, attributeCode, id);

        if (!doctypesService.isDoctypesValid(docTypeSrt)) {
            processValidationError(String.format("В документе №%d невалидное значение аттрибута %s", seqN, attributeCode), id);
        }
    }

    private void validateByDateTimeFormatter(Object object, int seqN, String attributeCode, DateTimeFormatter formatter, String id) {
        String objStr = nullOrBlankObjectValidationAndToString(object, seqN, attributeCode, id);

        if (object instanceof LocalDateTime ldt) {
            objStr = ldt.format(formatter);
        }

        try {
            formatter.parse(objStr);
        } catch (DateTimeParseException e) {
            processValidationError(String.format("В документе №%d в атрибут %s передано некорректное значение", seqN, attributeCode), id);
        }
    }

    private void validateDocAccount(Object docAccount, int seqN, String id) {
        String attributeCode = DocumentAttributeCode.DOC_ACCOUNT.getCode();
        String docAccountStr = nullOrBlankObjectValidationAndToString(docAccount, seqN, attributeCode, id);

        try {
            int docAccountInt = Integer.parseInt(docAccountStr);
            if (!departmentsService.isDepartmentValid(docAccountInt)) {
                processValidationError(String.format("В документе №%d невалидное значение аттрибута %s", seqN, attributeCode), id);
            }
        } catch (NumberFormatException e) {
            processValidationError(String.format("В документе №%d некорректное значение аттрибута %s", seqN, attributeCode), id);
        }
    }

    private void validateDocSourceSystem(Object docSourceSystem, int seqN, String id) {
        String attributeCode = DocumentAttributeCode.DOC_SOURCE_SYSTEM.getCode();
        String docSourceSystemStr = nullOrBlankObjectValidationAndToString(docSourceSystem, seqN, attributeCode, id);

        if (!sourceSystemsService.isSourceSystemValid(docSourceSystemStr)) {
            processValidationError(String.format("В документе №%d невалидное значение аттрибута %s", seqN, attributeCode), id);
        }
    }

    private void validateDocSum(Object docSum, int seqN, String id) {
        String attributeCode = DocumentAttributeCode.DOC_SUM.getCode();
        String docSumStr = nullOrBlankObjectValidationAndToString(docSum, seqN, attributeCode, id);

        try {
            double docSumDouble = Double.parseDouble(docSumStr);
            if (docSumDouble < 0) {
                processValidationError(String.format("В документе №%d в атрибут %s передано отрицательное значение", seqN, attributeCode), id);
            }
        } catch (NumberFormatException e) {
            processValidationError(String.format("В документе №%d в атрибут %s передано невалидное значение", seqN, attributeCode), id);
        }
    }

    private void validateDocSKOSymbol(Object docSKOSymbol, int seqN, String id) {
        String attributeCode = DocumentAttributeCode.DOC_SKO_SYMBOL.getCode();
        String docSKOSymbolStr = nullOrBlankObjectValidationAndToString(docSKOSymbol, seqN, attributeCode, id);
        int docSKOSymbolInt = parsingInt(docSKOSymbolStr, seqN, attributeCode, id);

        if (docSKOSymbolInt < 0) {
            processValidationError(String.format("В документе №%d в атрибут %s передано отрицательное значение", seqN, attributeCode), id);
        }
    }

    private void validateDocStatus(Object docStatus, int seqN, String id) {
        String attributeCode = DocumentAttributeCode.DOC_STATUS.getCode();
        String docStatusStr = nullOrBlankObjectValidationAndToString(docStatus, seqN, attributeCode, id);
        int docStatusInt = parsingInt(docStatusStr, seqN, attributeCode, id);

        if (docStatusInt != 0 && docStatusInt != 1) {
            processValidationError(String.format("В документе №%d в атрибут %s передано некорректное значение, допустимы 0 или 1", seqN, attributeCode), id);
        }
    }

    private int parsingInt(String value, int seqN, String attributeCode, String id) {
        int valueInt = 0;
        try {
            valueInt = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            processValidationError(String.format("В документе №%d в атрибут %s передано невалидное значение ", seqN, attributeCode), id);
        }
        return valueInt;
    }

    private void validateDocSigns(Object object, int seqN, String attributeCode, String id) {
        String objectStr = nullOrBlankObjectValidationAndToString(object, seqN, attributeCode, id);

        if (!FULL_NAME_PATTERN.matcher(objectStr).matches()) {
            processValidationError(String.format("В документе №%d в атрибут %s передано невалидное значение", seqN, attributeCode), id);
        }
    }

    private void validateDocumentBody(DocumentBody documentBody, int seqN, String id) {
        if (documentBody == null) {
            processValidationError(String.format("В документе №%d не передан тэг DocumentBody", seqN), id);
        }

        validateContent(documentBody.getContent(), seqN, id);
    }

    private void validateContent(List<Content> contentList, int seqN, String id) {
        if (contentList.isEmpty()) {
            processValidationError(String.format("В документе №%d в тэг DocumentBody не передан тэг Content", seqN), id);
        }

        for (Content content : contentList) {
            validateContentFormat(content.getFormat(), seqN, id);
            validateDocumentSings(content.getDocumentSigns(), seqN, id);
        }
    }

    private void validateContentFormat(String contentFormat, int seqN, String id) {
        String tag = "Content";
        String attribute = "Format";

        nullOrBlankWithSeqNFormatValidation(contentFormat, tag, attribute, seqN, id);

        if (!ContentFormat.isValid(contentFormat)) {
            processValidationError(String.format("В документе №%d в тэге %s в атрибут %s передано некорректное значение", seqN, tag, attribute), id);
        }
    }

    private void validateDocumentSings(DocumentSigns documentSigns, int seqN, String id) {
        if (documentSigns == null) {
            processValidationError(String.format("В документе №%d в тэг Content не передан тэг DocumentSigns", seqN), id);
        }

        validateSign(documentSigns.getSign(), seqN, id);
    }

    private void validateSign(List<Sign> sign, int seqN, String id) {
        if (sign.isEmpty()) {
            processValidationError(String.format("В документе №%d в тэг DocumentSigns не передан тэг Sign", seqN), id);
        }

        for (Sign signs : sign) {
            validateSignData(signs.getSignData(), seqN, id);
        }
    }

    private void validateSignData(SignBody signData, int seqN, String id) {
        if (signData == null) {
            processValidationError(String.format("В документе №%d в тэг Sign не передан тэг SignData", seqN), id);
        }

        validateSignDataFormat(signData.getFormat(), seqN, id);
    }

    private void validateSignDataFormat(String signDataFormat, int seqN, String id) {
        String tag = "SignData";
        String attribute = "Format";

        nullOrBlankWithSeqNFormatValidation(signDataFormat, tag, attribute, seqN, id);

        if (!SignDataFormat.isValid(signDataFormat)) {
            processValidationError(String.format("В документе №%d в тэге %s в атрибут %s передано невалидное значение", seqN, tag, attribute), id);
        }
    }

    private void processValidationError(String message, String id) {
        if (id == null || id.isBlank()) {
            log.error("{}: {} Для текущего запроса в БД не создана выгрузка со статусом -1 т.к. ID пуст или не передан", ERROR_MESSAGE, message);
        } else {
            int inserted = unloadingCrudService.setUnloadingStateId(id, UnloadingState.UNLOADING_ERROR);
            if (inserted > 0) {
                log.error("{}: {}. Для запроса с ID={} в БД создана выгрузка со статусом -1", ERROR_MESSAGE, message, id);
            } else {
                log.error("{}: {}. Для запроса с ID={} в БД не создана выгрузка со статусом -1 т.к. выгрузка с таким ID уже существует", ERROR_MESSAGE, message, id);
            }
        }
        throw new ValidationException(ERROR_MESSAGE);
    }

    private void validateNotNull(Object object, String attributeCode, String id) {
        if (object == null) {
            processValidationError(String.format("Атрибут %s не передан или передан некорректный формат", attributeCode), id);
        }
    }

    private void validateIsPositive(int value, String attributeCode, String id) {
        if (value <= 0) {
            processValidationError(String.format("Атрибут %s не передан или передано некорректное значение", attributeCode), id);
        }
    }

    private void nullOrBlankAttributeValidation(String attributeValue, String attribute, String id) {
        if (attributeValue == null) {
            processValidationError(String.format("Атрибут %s не передан", attribute), id);
        } else if (attributeValue.isBlank()) {
            processValidationError(String.format("В атрибут %s передано пустое значение", attribute), id);
        }
    }

    private void nullOrBlankWithSeqNFormatValidation(String format, String tag, String attribute, int seqN, String id) {
        if (format == null) {
            processValidationError(String.format("В документе №%d в тэг %s не передан атрибут %s", seqN, tag, attribute), id);
        } else if (format.isBlank()) {
            processValidationError(String.format("В документе №%d в тэге %s в атрибут %s не передано значение", seqN, tag, attribute), id);
        }
    }

    private String nullOrBlankObjectValidationAndToString(Object object, int seqN, String attributeCode, String id) {
        if (object == null) {
            processValidationError(String.format("В документе №%d не передано значение атрибута \"%s\"", seqN, attributeCode), id);
        }

        String objectStr = object.toString();
        if (objectStr.isBlank()) {
            processValidationError(String.format("В документе №%d передано пустое значение атрибута %s", seqN, attributeCode), id);
        }

        return objectStr;
    }
}
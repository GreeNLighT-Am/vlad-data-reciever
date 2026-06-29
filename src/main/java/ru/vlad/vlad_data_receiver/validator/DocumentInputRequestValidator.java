package ru.vlad.vlad_data_receiver.validator;

import jakarta.annotation.PostConstruct;
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
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
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
    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{4}");
    private static final Pattern FULL_NAME_PATTERN = Pattern.compile(
            "^[А-ЯЁ][а-яё]+(?:-[А-ЯЁ][а-яё]+)?\\s" +
                    "[А-ЯЁ][а-яё]+(?:-[А-ЯЁ][а-яё]+)?" +
                    "\\s[А-ЯЁ][а-яё]+(?:-[А-ЯЁ][а-яё]+)?$"
    );
    private static final String ERROR_MESSAGE = "ErrorUnloadingEvent";

    private final Map<DocumentAttributeCodes, BiConsumer<Object, Integer>> validators = new EnumMap<>(DocumentAttributeCodes.class);

    @PostConstruct
    public void initValidators() {
        validators.put(DocumentAttributeCodes.DOC_TYPE, this::validateDocType);
        validators.put(DocumentAttributeCodes.DOC_NUMBER, this::validateDocNumber);
        validators.put(DocumentAttributeCodes.DOC_DATE, this::validateDocDate);
        validators.put(DocumentAttributeCodes.DOC_ACCOUNT, this::validateDocAccount);
        validators.put(DocumentAttributeCodes.DOC_SOURCE_SYSTEM, this::validateDocSourceSystem);
        validators.put(DocumentAttributeCodes.DOC_TIME_STAMP, this::validateDocTimeStamp);
        validators.put(DocumentAttributeCodes.DOC_SUM, this::validateDocSum);
        validators.put(DocumentAttributeCodes.DOC_STATUS, this::validateDocStatus);
        validators.put(DocumentAttributeCodes.DOC_SKO_SYMBOL, this::validateDocSKOSymbol);
        validators.put(DocumentAttributeCodes.DOC_SIGN_1, this::validateDocSign1);
        validators.put(DocumentAttributeCodes.DOC_SIGN_2, this::validateDocSign2);
    }

    private void processValidationError(String message) {
        log.error(message);
        throw new ValidationException(ERROR_MESSAGE);
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
            processValidationError(String.format("В документе №%d не передан аттрибут AttributeValue для AttributeCode=\"%s\"", seqN, attributeCode));
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

        validateId(documentInputRequest.getID());

        validateTimeStamp(documentInputRequest.getTimeStamp());

        int blockNum = documentInputRequest.getBlockNum();
        validateBlockNum(blockNum);

        int totalDocs = documentInputRequest.getTotalDocs();
        validateTotalDocs(totalDocs);

        validateIsBlockNumMoreThenTotalDocs(blockNum, totalDocs);

        validateDataSet(documentInputRequest.getDataSet());

        validateOdDocType(documentInputRequest.getOdDocType());
    }

    private void validateId(String id) {
        String attributeName = "ID";
        nullOrBlankAttributeValidation(id, attributeName);
    }

    private void validateTimeStamp(LocalDateTime timeStamp) {
        if (timeStamp == null) {
            processValidationError("Атрибут TimeStamp не передан или передан некорректный формат");
        }
    }

    private void validateBlockNum(int blockNum) {
        if (blockNum <= 0) {
            processValidationError("Атрибут blockNum не передан или передано некорректное значение");
        }
    }

    private void validateTotalDocs(int totalDocs) {
        if (totalDocs <= 0) {
            processValidationError("Атрибут totalDocs не передан или передано некорректное значение");
        }
    }

    private void validateIsBlockNumMoreThenTotalDocs(int blockNum, int totalDocs) {
        if (blockNum > totalDocs) {
            processValidationError("Значение атрибута blockNum больше значения атрибута totalDocs");
        }
    }

    private void validateDataSet(String dataSet) {
        String attributeName = "dataSet";
        nullOrBlankAttributeValidation(dataSet, attributeName);
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
        } else if (totalDocs > documents.size()) {
            processValidationError("Передано меньше документов чем ожидается");
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

            BiConsumer<Object, Integer> validator = validators.get(attrCode);
            if (validator != null) {
                validator.accept(attrValue, seqN);
            }
        }

        Set<String> missingAttributes = new HashSet<>(requiredAttributes);
        missingAttributes.removeAll(foundAttributes);

        if (!missingAttributes.isEmpty()) {
            processValidationError(String.format("В документе №%d для обязательного AttributeCode: %s передано некорректное значение", seqN, missingAttributes));
        }
    }

    private void validateDocType(Object docType, int seqN) {
        String attributeCode = DocumentAttributeCodes.DOC_TYPE.getCode();
        String docTypeSrt = nullOrBlankObjectValidationAndToString(docType, seqN, attributeCode);

        if (!doctypesService.isDoctypesValid(docTypeSrt)) {
            processValidationError(String.format("В документе №%d невалидное значение аттрибута %s", seqN, attributeCode));
        }
    }

    private void validateDocNumber(Object docNumber, int seqN) {
        String attributeCode = DocumentAttributeCodes.DOC_NUMBER.getCode();
        nullOrBlankObjectValidationAndToString(docNumber, seqN, attributeCode);
    }

    private void validateDocDate(Object docDate, int seqN) {
        String attributeCode = DocumentAttributeCodes.DOC_DATE.getCode();
        String docDateStr = nullOrBlankObjectValidationAndToString(docDate, seqN, attributeCode);

        if (docDate instanceof LocalDateTime ldt) {
            docDateStr = ldt.format(DATE_FORMATTER);
        }

        if (!DATE_PATTERN.matcher(docDateStr).matches()) {
            processValidationError(String.format("В документе №%d, в параметр %s передано невалидное значение", seqN, attributeCode));
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

    private void validateDocTimeStamp(Object docTimeStamp, int seqN) {
        String attributeCode = DocumentAttributeCodes.DOC_TIME_STAMP.getCode();
        String timestampStr = nullOrBlankObjectValidationAndToString(docTimeStamp, seqN, attributeCode);

        if (docTimeStamp instanceof LocalDateTime ldt) {
            timestampStr = ldt.format(TIMESTAMP_FORMATTER);
        }

        try {
            TIMESTAMP_FORMATTER.parse(timestampStr);
        } catch (DateTimeParseException e) {
            processValidationError(String.format("В документе №%d в атрибут %s передано некорректное значение", seqN, attributeCode));
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

    private void validateDocStatus(Object docStatus, int seqN) {
        String attributeCode = DocumentAttributeCodes.DOC_STATUS.getCode();
        String docStatusStr = nullOrBlankObjectValidationAndToString(docStatus, seqN, attributeCode);

        try {
            int docStatusInt = Integer.parseInt(docStatusStr);

            if (!(docStatusInt == 0) && !(docStatusInt == 1)) {
                processValidationError(String.format("В документе №%d в атрибут %s передано некорректное значение", seqN, attributeCode));
            }
        } catch (NumberFormatException e) {
            processValidationError(String.format("В документе №%d в атрибут %s передано невалидное значение ", seqN, attributeCode));
        }
    }

    private void validateDocSKOSymbol(Object docSKOSymbol, int seqN) {
        String attributeCode = DocumentAttributeCodes.DOC_SKO_SYMBOL.getCode();
        String docSKOSymbolStr = nullOrBlankObjectValidationAndToString(docSKOSymbol, seqN, attributeCode);

        try {
            int docSKOSymbolInt = Integer.parseInt(docSKOSymbolStr);
            if (docSKOSymbolInt < 0) {
                processValidationError(String.format("В документе №%d в атрибут %s передано отрицательное значение", seqN, attributeCode));
            }
        } catch (NumberFormatException e) {
            processValidationError(String.format("В документе №%d в атрибут %s передано невалидное значение", seqN, attributeCode));
        }
    }

    private void validateDocSign1(Object docSign1, int seqN) {
        String attributeCode = DocumentAttributeCodes.DOC_SKO_SYMBOL.getCode();
        String docSign1Str = nullOrBlankObjectValidationAndToString(docSign1, seqN, attributeCode);

        if (!FULL_NAME_PATTERN.matcher(docSign1Str).matches()) {
            processValidationError(String.format("В документе №%d в атрибут %s передано невалидное значение", seqN, attributeCode));
        }
    }

    private void validateDocSign2(Object docSign2, int seqN) {
        String attributeCode = DocumentAttributeCodes.DOC_SKO_SYMBOL.getCode();
        String docSign2Str = nullOrBlankObjectValidationAndToString(docSign2, seqN, attributeCode);

        if (!FULL_NAME_PATTERN.matcher(docSign2Str).matches()) {
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
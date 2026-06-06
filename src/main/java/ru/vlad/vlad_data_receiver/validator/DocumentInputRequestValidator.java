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
            "^[А-ЯЁ][а-яё]+(?:-[А-ЯЁ][а-яё]+)?\\s" +        // Фамилия с возможным дефисом
                    "[А-ЯЁ][а-яё]+(?:-[А-ЯЁ][а-яё]+)?" +    // Имя с возможным дефисом
                    "\\s[А-ЯЁ][а-яё]+(?:-[А-ЯЁ][а-яё]+)?$"  // Отчество с возможным дефисом
    );
    private static final String ERROR_MESSAGE = "ErrorUnloadingEvent";

    private final Map<DocumentAttributeCodes, BiConsumer<Object, Integer>> validators = new EnumMap<>(DocumentAttributeCodes.class);

    @PostConstruct
    public void initValidators() {
        validators.put(DocumentAttributeCodes.DocType, this::validateDocType);
        validators.put(DocumentAttributeCodes.DocNumber, this::validateDocNumber);
        validators.put(DocumentAttributeCodes.DocDate, this::validateDocDate);
        validators.put(DocumentAttributeCodes.DocAccount, this::validateDocAccount);
        validators.put(DocumentAttributeCodes.DocSourceSystem, this::validateDocSourceSystem);
        validators.put(DocumentAttributeCodes.DocTimeStamp, this::validateDocTimeStamp);
        validators.put(DocumentAttributeCodes.DocSum, this::validateDocSum);
        validators.put(DocumentAttributeCodes.DocStatus, this::validateDocStatus);
        validators.put(DocumentAttributeCodes.DocSKOSymbol, this::validateDocSKOSymbol);
        validators.put(DocumentAttributeCodes.DocSign1, this::validateDocSign1);
        validators.put(DocumentAttributeCodes.DocSign2, this::validateDocSign2);
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
        if (id == null) {
            log.error("Атрибут ID не передан");
            throw new ValidationException(ERROR_MESSAGE);
        } else if (id.isBlank()) {
            log.error("Передано пустое значение в атрибуте ID");
            throw new ValidationException(ERROR_MESSAGE);
        }
    }

    private void validateTimeStamp(LocalDateTime timeStamp) {
        if (timeStamp == null) {
            log.error("Атрибут TimeStamp не передан или передан некорректный формат");
            throw new ValidationException(ERROR_MESSAGE);
        }
    }

    private void validateBlockNum(int blockNum) {
        if (blockNum <= 0) {
            log.error("Атрибут blockNum не передан или передано некорректное значение");
            throw new ValidationException(ERROR_MESSAGE);
        }
    }

    private void validateTotalDocs(int totalDocs) {
        if (totalDocs <= 0) {
            log.error("Атрибут totalDocs не передан или передано некорректное значение");
            throw new ValidationException(ERROR_MESSAGE);
        }
    }

    private void validateIsBlockNumMoreThenTotalDocs(int blockNum, int totalDocs) {
        if (blockNum > totalDocs) {
            log.error("Значение атрибута blockNum больше значения атрибута totalDocs");
            throw new ValidationException(ERROR_MESSAGE);
        }
    }

    private void validateDataSet(String dataSet) {
        if (dataSet == null) {
            log.error("Атрибут dataSet не передан");
            throw new ValidationException(ERROR_MESSAGE);
        } else if (dataSet.isBlank()) {
            log.error("Передано пустое значение в атрибуте dataSet");
            throw new ValidationException(ERROR_MESSAGE);
        }
    }

    private void validateOdDocType(String odDocType) {
        if (odDocType == null) {
            log.error("Атрибут odDocType не передан");
            throw new ValidationException(ERROR_MESSAGE);
        } else if (odDocType.isBlank()) {
            log.error("В атрибут odDocType передано пустое значение ");
            throw new ValidationException(ERROR_MESSAGE);
        }
        try {
            OdDocTypes.valueOf(odDocType);
        } catch (IllegalArgumentException e) {
            log.error("В атрибут odDocType передано невалидное значение ");
            throw new ValidationException(ERROR_MESSAGE);
        }
    }

    private void validateDocuments(List<Document> documents, int totalDocs) {
        if (documents.isEmpty()) {
            log.error("Не обнаружен ни один тэг Document)");
            throw new ValidationException(ERROR_MESSAGE);
        } else if (totalDocs > documents.size()) {
            log.error("Передано меньше документов чем ожидается");
            throw new ValidationException(ERROR_MESSAGE);
        }

        for (Document Document : documents) {
            validateDocument(Document, totalDocs);
        }
    }

    private void validateDocument(Document document, int totalDocs) {
        int seqN = document.getSeqN();

        if (totalDocs > 1 && seqN <= 0) {
            log.error("В одном или нескольких тэгах Document не передан атрибут SeqN или передано не валидное значение");
            throw new ValidationException(ERROR_MESSAGE);
        } else if (totalDocs > 1 && seqN > totalDocs) {
            log.error("В одном или нескольких тэгах Document передан атрибут SeqN со значением превышающим значение атрибута totalDocs");
            throw new ValidationException(ERROR_MESSAGE);
        } else if (totalDocs == 1 && seqN <= 0) {
            seqN = 1;
        }

        validateDocumentCard(document.getDocumentCard(), seqN);
        validateDocumentBody(document.getDocumentBody(), seqN);
    }

    private void validateDocumentCard(DocumentCard documentCard, int seqN) {
        if (documentCard == null) {
            log.error("В документе №{} не передан тэг DocumentCard", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }

        validateVariableAttributes(documentCard.getVariableAttribute(), seqN);
    }

    private void validateVariableAttributes(List<DocumentAttribute> attributesList, int seqN) {
        if (attributesList.isEmpty()) {
            log.error("В документе №{}, в тэг DocumentCard не передан ни один тэг VariableAttribute", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }

        Set<String> requiredAttributes = DocumentAttributeCodes.getRequiredCodes();
        Set<String> foundAttributes = new HashSet<>();

        for (DocumentAttribute attr : attributesList) {
            String attrCodeStr = attr.getAttributeCode();
            Object attrValue = attr.getAttributeValue();

            if (attrCodeStr == null) {
                log.error("В документе №{} в одном или нескольких тегах VariableAttribute не передан атрибут AttributeCode", seqN);
                throw new ValidationException(ERROR_MESSAGE);
            } else if (attrCodeStr.isBlank()) {
                log.error("В документе №{}, в одном или нескольких тегах VariableAttribute в атрибут AttributeCode передано пустое значение", seqN);
                throw new ValidationException(ERROR_MESSAGE);
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
            log.error("В документе №{} для обязательного AttributeCode: {} передано некорректное значение", seqN, missingAttributes);
            throw new ValidationException(ERROR_MESSAGE);
        }
    }

    private void validateDocType(Object DocType, int seqN) {
        if (DocType == null) {
            log.error("В документе №{} не передан аттрибут AttributeValue для AttributeCode=\"DocType\"", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        } else if (DocType.toString().isBlank()) {
            log.error("В документе №{} не передано значение аттрибута AttributeValue в AttributeCode=\"DocType\"", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        } else if (!doctypesService.isDoctypesValid(DocType.toString())) {
            log.error("В документе №{} не валидное значение аттрибута AttributeValue для AttributeCode=\"DocType\"", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }
    }

    private void validateDocNumber(Object docNumberValue, int seqN) {
        if (docNumberValue == null) {
            log.error("В документе №{} не передан аттрибут AttributeValue для AttributeCode=\"DocNumber\"", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        } else if (docNumberValue.toString().isBlank()) {
            log.error("В документе №{} не передано значение аттрибута AttributeValue в AttributeCode=\"DocNumber\"", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }
    }

    private void validateDocDate(Object docDate, int seqN) {
        if (docDate == null) {
            log.error("В документе №{} не передан аттрибут AttributeValue для AttributeCode=\"DocDate\"", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }

        String dateStr = docDate.toString();

        if (dateStr.isBlank()) {
            log.error("В документе №{} значение AttributeValue для AttributeCode=\"DocDate\" передано пустым", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        } else if (docDate instanceof LocalDateTime ldt) {
            dateStr = ldt.format(DATE_FORMATTER);
        }

        if (!DATE_PATTERN.matcher(dateStr).matches()) {
            log.error("В документе №{}, в параметр AttributeValue для AttributeCode=\"DocDate\" передано невалидное значение", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }
    }

    private void validateDocAccount(Object docAccount, int seqN) {
        if (docAccount == null) {
            log.error("В документе №{} не передан аттрибут AttributeValue для AttributeCode=\"DocAccount\"", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }

        String docAccountStr = docAccount.toString();

        if (docAccountStr.isBlank()) {
            log.error("В документе №{} не передано значение AttributeValue в AttributeCode=\"DocAccount\"", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        } else if (!departmentsService.isDepartmentValid(docAccountStr)) {
            log.error("В документе №{} невалидное значение аттрибута AttributeValue для AttributeCode=\"DocAccount\"", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }
    }

    private void validateDocSourceSystem(Object docSourceSystem, int seqN) {
        if (docSourceSystem == null) {
            log.error("В документе №{} не передан аттрибут AttributeValue для AttributeCode=\"SourceSystem\"", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }

        String docSourceSystemStr = docSourceSystem.toString();

        if (docSourceSystemStr.isBlank()) {
            log.error("В документе №{} не передано значение аттрибута AttributeValue в AttributeCode=\"DocSourceSystem\"", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        } else if (!sourceSystemsService.isSourceSystemValid(docSourceSystemStr)) {
            log.error("В документе №{} невалидное значение аттрибута AttributeValue для AttributeCode=\"DocSourceSystem\"", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }
    }

    private void validateDocTimeStamp(Object docTimeStamp, int seqN) {
        if (docTimeStamp == null) {
            log.error("В документе №{} не передан атрибут AttributeValue аттрибута AttributeCode=\"DocTimeStamp\"", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }

        String timestampStr = docTimeStamp.toString();
        if (timestampStr.isBlank()) {
            log.error("В документе №{} в атрибут AttributeValue атрибута AttributeCode=\"DocTimeStamp\" передано пустое значение", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        } else if (docTimeStamp instanceof LocalDateTime ldt) {
            timestampStr = ldt.format(TIMESTAMP_FORMATTER);
        }

        try {
            TIMESTAMP_FORMATTER.parse(timestampStr);
        } catch (DateTimeParseException e) {
            log.error("В документе №{} в атрибут AttributeValue для AttributeCode=\"DocTimeStamp\" передано некорректное значение", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }
    }

    private void validateDocSum(Object docSum, int seqN) {
        if (docSum == null) {
            log.error("В документе №{} не передан аттрибут AttributeValue для AttributeCode=\"DocSum\"", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }

        String docSumStr = docSum.toString();
        if (docSumStr.isBlank()) {
            log.error("В документе №{} не передано значение аттрибута AttributeValue для AttributeCode=\"DocSum\"", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }

        try {
            double docSumDouble = Double.parseDouble(docSumStr);
            if (docSumDouble < 0) {
                log.error("В документе №{} в атрибут AttributeValue для AttributeCode=\"DocSum\" передано отрицательное значение", seqN);
                throw new ValidationException(ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            log.error("В документе №{} в атрибут AttributeValue для AttributeCode=\"DocSum\" передано невалидное значение",
                    seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }
    }

    private void validateDocStatus(Object docStatus, int seqN) {
        if (docStatus == null) {
            log.error("В документе №{} не передан аттрибут AttributeValue для AttributeCode=\"DocStatus\"", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }

        String docStatusStr = docStatus.toString();
        if (docStatusStr.isBlank()) {
            log.error("В документе №{} не передано значение атрибута AttributeValue для AttributeCode=\"DocStatus\"", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }

        try {
            int docStatusInt = Integer.parseInt(docStatusStr);

            if (!(docStatusInt == 0) && !(docStatusInt == 1)) {
                log.error("В документе №{} в атрибут AttributeValue для AttributeCode=\"DocStatus\" передано некорректное значение", seqN);
                throw new ValidationException(ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            log.error("В документе №{} в атрибут AttributeValue для AttributeCode=\"DocStatus\" передано невалидное значение ", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }
    }

    private void validateDocSKOSymbol(Object docSKOSymbol, int seqN) {
        if (docSKOSymbol == null) {
            log.error("В документе №{} не передан атрибут AttributeValue для AttributeCode=\"DocSKOSymbol\"", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }

        String docSKOSymbolStr = docSKOSymbol.toString();
        if (docSKOSymbolStr.isBlank()) {
            log.error("В документе №{} не передано значение атрибута AttributeValue для AttributeCode=\"DocSKOSymbol\"", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }

        try {
            int docSKOSymbolInt = Integer.parseInt(docSKOSymbolStr);
            if (docSKOSymbolInt < 0) {
                log.error("В документе №{} в атрибут AttributeValue для AttributeCode=\"DocSKOSymbol\" передано отрицательное значение", seqN);
                throw new ValidationException(ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            log.error("В документе №{} в атрибут AttributeValue для AttributeCode=\"DocSKOSymbol\" передано невалидное значение",
                    seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }
    }

    private void validateDocSign1(Object docSign1, int seqN) {
        if (docSign1 == null) {
            log.error("В документе №{} не передан атрибут AttributeValue для AttributeCode=\"DocSign1\"", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }

        String docSign1Str = docSign1.toString();
        if (docSign1Str.isBlank()) {
            log.error("В документе №{} не передано значение атрибута AttributeValue в AttributeCode=\"DocSign1\"", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }

        if (!FULL_NAME_PATTERN.matcher(docSign1Str).matches()) {
            log.error("В документе №{} в атрибут AttributeValue для AttributeCode=\"DocSign1\" передано невалидное значение", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }
    }

    private void validateDocSign2(Object docSign2, int seqN) {
        if (docSign2 == null) {
            log.error("В документе №{} не передан атрибут AttributeValue для AttributeCode=\"DocSign2\"", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }

        String docSign2Str = docSign2.toString();
        if (docSign2Str.isBlank()) {
            log.error("В документе №{} не передано значение атрибута AttributeValue в AttributeCode=\"DocSign2\"", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }

        if (!FULL_NAME_PATTERN.matcher(docSign2Str).matches()) {
            log.error("В документе №{} в атрибут AttributeValue для AttributeCode=\"DocSign2\" передано невалидное значение", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }
    }

    private void validateDocumentBody(DocumentBody documentBody, int seqN) {
        if (documentBody == null) {
            log.error("В документе №{} не передан тэг DocumentBody", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }

        validateContent(documentBody.getContent(), seqN);
    }

    private void validateContent(List<Content> contentList, int seqN) {
        if (contentList.isEmpty()) {
            log.error("В документе №{} в тэг DocumentBody не передан тэг Content", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }

        for (Content content : contentList) {
            validateContentFormat(content.getFormat(), seqN);
            validateDocumentSings(content.getDocumentSigns(), seqN);
        }
    }

    private void validateContentFormat(String contentFormat, int seqN) {
        if (contentFormat == null) {
            log.error("В документе №{} в тэг Content не передан атрибут Format", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        } else if (contentFormat.isBlank()) {
            log.error("В документе №{} в тэге Content в атрибут Format не передано значение", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }

        try {
            ContentFormats.valueOf(contentFormat.toLowerCase());
        } catch (IllegalArgumentException e) {
            log.error("В документе №{} в тэге Content в атрибут Format передано некорректное значение", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }
    }

    private void validateDocumentSings(DocumentSigns documentSigns, int seqN) {
        if (documentSigns == null) {
            log.error("В документе №{} тэг Content не передан тэг DocumentSigns", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }

        validateSign(documentSigns.getSign(), seqN);
    }

    private void validateSign(List<Sign> sign, int seqN) {
        if (sign.isEmpty()) {
            log.error("В документе №{} в тэг DocumentSigns не передан тэг Sign", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }

        for (Sign signs : sign) {
            validateSignData(signs.getSignData(), seqN);
        }
    }

    private void validateSignData(SignBody signData, int seqN) {
        if (signData == null) {
            log.error("В документе №{} в тэг Sign не передан тэг SignData", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }

        validateSignDataFormat(signData.getFormat(), seqN);
    }

    private void validateSignDataFormat(String format, int seqN) {
        if (format == null) {
            log.error("В документе №{} в тэг SignData не передан атрибут Format", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        } else if (format.isBlank()) {
            log.error("В документе №{} в тэге SignData в атрибут Format передано пустое значение", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }

        try {
            SignDataFormats.valueOf(format.toLowerCase());
        } catch (IllegalArgumentException e) {
            log.error("В документе №{} в тэге SignData в атрибут Format передано невалидное значение", seqN);
            throw new ValidationException(ERROR_MESSAGE);
        }
    }
}
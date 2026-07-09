package ru.vlad.vlad_data_receiver.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.vlad.vlad_data_receiver.entity.DocumentEntity;
import ru.vlad.vlad_data_receiver.model.constants.DocumentAttributeCodes;
import ru.vlad.vlad_data_receiver.parser.documents.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper(
        componentModel = "spring",
        imports = {DocumentAttributeCodes.class}
)
public interface DocumentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bundleId", source = "bundleId")
    @Mapping(target = "unloadingRequestId", source = "unloadingRequestId")
    @Mapping(target = "timestamp", source = "documentTimeStamp")
    @Mapping(target = "od_p", source = "operationalDayDate")
    @Mapping(target = "format", expression = "java(document.getDocumentBody().getContent().get(0).getFormat())")
    @Mapping(target = "doctypeCode", expression = "java(getAttributeValue(document, DocumentAttributeCodes.DOC_TYPE))")
    @Mapping(target = "number", expression = "java(getAttributeValue(document, DocumentAttributeCodes.DOC_NUMBER))")
    @Mapping(target = "departmentCode", expression = "java(getAttributeIntValue(document, DocumentAttributeCodes.DOC_ACCOUNT))")
    @Mapping(target = "docSum", expression = "java(getAttributeBigDecimal(document, DocumentAttributeCodes.DOC_SUM))")
    @Mapping(target = "sign1", expression = "java(getAttributeValue(document, DocumentAttributeCodes.DOC_SIGN_1))")
    @Mapping(target = "sign2", expression = "java(getAttributeValue(document, DocumentAttributeCodes.DOC_SIGN_2))")
    @Mapping(target = "sign3", expression = "java(getAttributeValue(document, DocumentAttributeCodes.DOC_SIGN_3))")
    DocumentEntity toDocumentEntity(
            Document document,
            LocalDateTime documentTimeStamp,
            Long bundleId,
            String unloadingRequestId,
            LocalDate operationalDayDate
    );

    default String getAttributeValue(Document document, DocumentAttributeCodes code) {
        return document.getDocumentCard().getVariableAttribute().stream()
                .filter(attr -> code.getCode().equals(attr.getAttributeCode()))
                .findFirst()
                .map(attr -> attr.getAttributeValue().toString())
                .orElse(null);
    }

    default Integer getAttributeIntValue(Document document, DocumentAttributeCodes code) {
        String value = getAttributeValue(document, code);
        return value != null ? Integer.parseInt(value) : null;
    }

    default BigDecimal getAttributeBigDecimal(Document document, DocumentAttributeCodes code) {
        String value = getAttributeValue(document, code);
        return value != null ? new BigDecimal(value) : null;
    }
}
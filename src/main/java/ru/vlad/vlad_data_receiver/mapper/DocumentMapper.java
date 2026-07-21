package ru.vlad.vlad_data_receiver.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.vlad.vlad_data_receiver.repository.entity.DocumentEntity;
import ru.vlad.vlad_data_receiver.model.constants.DocumentAttributeCodes;
import ru.vlad.vlad_data_receiver.parser.documents.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import ru.vlad.vlad_data_receiver.util.DocumentAttributeExtractor;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface DocumentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timestamp", source = "documentTimeStamp")
    @Mapping(target = "od_p", source = "operationalDayDate")
    @Mapping(target = "format", source = "document", qualifiedByName = "toFormat")
    @Mapping(target = "doctypeCode", source = "document", qualifiedByName = "toDocTypeCode")
    @Mapping(target = "number", source = "document", qualifiedByName = "toNumber")
    @Mapping(target = "departmentCode", source = "document", qualifiedByName = "toDepartmentCode")
    @Mapping(target = "docSum", source = "document", qualifiedByName = "toDocSum")
    @Mapping(target = "sign1", source = "document", qualifiedByName = "toSign1")
    @Mapping(target = "sign2", source = "document", qualifiedByName = "toSign2")
    @Mapping(target = "sign3", source = "document", qualifiedByName = "toSign3")
    DocumentEntity toDocumentEntity(
            Document document,
            LocalDateTime documentTimeStamp,
            Long bundleId,
            String unloadingRequestId,
            LocalDate operationalDayDate
    );

    @Named("toFormat")
    static String toFormat(Document document) {
        return document.getDocumentBody().getContent().get(0).getFormat();
    }

    @Named("toDocTypeCode")
    static String toDocTypeCode(Document document) {
        return DocumentAttributeExtractor.getAttributeValue(document, DocumentAttributeCodes.DOC_TYPE);
    }

    @Named("toNumber")
    static String toNumber(Document document) {
        return DocumentAttributeExtractor.getAttributeValue(document, DocumentAttributeCodes.DOC_NUMBER);
    }

    @Named("toDepartmentCode")
    static Integer toDepartmentCode(Document document) {
        return DocumentAttributeExtractor.getAttributeAsInteger(document, DocumentAttributeCodes.DOC_ACCOUNT);
    }

    @Named("toDocSum")
    static BigDecimal toDocSum(Document document) {
        return DocumentAttributeExtractor.getAttributeAsBigDecimal(document, DocumentAttributeCodes.DOC_SUM);
    }

    @Named("toSign1")
    static String toSign1(Document document) {
        return DocumentAttributeExtractor.getAttributeValue(document, DocumentAttributeCodes.DOC_SIGN_1);
    }

    @Named("toSign2")
    static String toSign2(Document document) {
        return DocumentAttributeExtractor.getAttributeValue(document, DocumentAttributeCodes.DOC_SIGN_2);
    }

    @Named("toSign3")
    static String toSign3(Document document) {
        return DocumentAttributeExtractor.getAttributeValue(document, DocumentAttributeCodes.DOC_SIGN_3);
    }
}
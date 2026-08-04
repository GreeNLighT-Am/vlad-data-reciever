package ru.vlad.vlad_data_receiver.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.vlad.vlad_data_receiver.model.constants.UnloadingState;
import ru.vlad.vlad_data_receiver.parser.documents.DocumentCard;
import ru.vlad.vlad_data_receiver.repository.entity.UnloadingEntity;
import ru.vlad.vlad_data_receiver.util.DocumentAttributeExtractor;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface UnloadingMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sourceSystemCode", source = "firstDocCard", qualifiedByName = "toSourceSystemCode")
    @Mapping(target = "stateId", source = "firstDocCard", qualifiedByName = "toStateId")
    @Mapping(target = "departmentNumber", source = "firstDocCard", qualifiedByName = "toDepartmentNumber")
    UnloadingEntity toUnloadingEntity(
            String unloadingRequestId,
            LocalDateTime date,
            Integer totalDocs,
            Long operationalDayId,
            String docCategory,
            DocumentCard firstDocCard
    );

    @Named("toSourceSystemCode")
    static String toSourceSystemCode(DocumentCard firstDocCard) {
        return DocumentAttributeExtractor.getSourceSystemCode(firstDocCard);
    }

    @Named("toDepartmentNumber")
    static Integer toDepartmentNumber(DocumentCard firstDocCard) {
        return DocumentAttributeExtractor.getDepartmentNumber(firstDocCard);
    }

    @Named("toStateId")
    static int toStateId(DocumentCard firstDocCard) {
        return UnloadingState.NEW_UNLOADING.getStateId();
    }
}
package ru.vlad.vlad_data_receiver.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "unloading", schema = "vlad_db")
public class UnloadingEntity {
    @Id
    private Long id;

    private String unloadingRequestId;

    private String sourceSystemCode;

    private LocalDateTime date;

    private Integer totalDocs;

    private Long stateId;

    private Long operationalDayId;

    private Integer departmentNumber;

    private String docCategory;
}
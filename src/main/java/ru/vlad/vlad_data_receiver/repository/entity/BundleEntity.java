package ru.vlad.vlad_data_receiver.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bundle", schema = "vlad_db")
public class BundleEntity {
    @Id
    private Long id;

    private String status;

    private Integer documentCount;

    private Integer bundleNum;

    private String unloadingRequestId;

    private Integer type;

    private LocalDate od_p;
}
package ru.vlad.vlad_data_receiver.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "source_systems", schema = "vlad_db")
public class SourceSystemsEntity {
    @Id
    private String code;
    private String name;
    private Boolean isManual;
}
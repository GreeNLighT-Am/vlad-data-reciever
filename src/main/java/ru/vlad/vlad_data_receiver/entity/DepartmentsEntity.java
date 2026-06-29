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
@Table(name = "departments", schema = "vlad_db")
public class DepartmentsEntity {
    @Id
    private Integer code;

    private String name;

    private LocalDateTime createDate;

    private LocalDateTime modifyDate;

    private Boolean isActive;
}

package ru.vlad.vlad_data_receiver.entity;

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
@Table(name = "departments", schema = "vlad_db")
public class DepartmentsEntity {
    @Id
    private String code;

    private String name;

    private LocalDate create_date;

    private LocalDate modify_date;

    private Integer is_active;
}

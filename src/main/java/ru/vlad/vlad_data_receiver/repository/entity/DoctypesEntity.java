package ru.vlad.vlad_data_receiver.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "doctypes", schema = "vlad_db")
public class DoctypesEntity {
    @Id
    private String code;
    private String name;
    private Long groupId;
    private BigDecimal lifeTime;
}
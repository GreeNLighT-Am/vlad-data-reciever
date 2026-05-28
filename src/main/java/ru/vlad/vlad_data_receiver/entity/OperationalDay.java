package ru.vlad.vlad_data_receiver.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "operational_day", schema = "vlad_db")
public class OperationalDay {
    @Id
    @Column("id")
    private Long id;

    @Column("date")
    private LocalDate date;

    @Column("state_id")
    private Long stateId;
}

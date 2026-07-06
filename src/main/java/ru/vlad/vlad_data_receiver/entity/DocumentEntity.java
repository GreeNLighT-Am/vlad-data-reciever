package ru.vlad.vlad_data_receiver.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "document", schema = "vlad_db")
public class DocumentEntity {
    @Id
    private Long id;

    String doctypeCode;

    private String number;

    private String format;

    private Long bundleId;

    private LocalDateTime timestamp;

    private Integer departmentCode;

    private BigDecimal docSum;

    private String sign1;

    private String sign2;

    private String sign3;

    private String unloadingRequestId;

    private LocalDate od_p;
}
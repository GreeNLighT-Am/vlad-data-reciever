package ru.vlad.vlad_data_receiver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vlad.vlad_data_receiver.entity.OperationalDayEntity;
import ru.vlad.vlad_data_receiver.repository.OperationalDayRepository;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationalDayCrudService {

    private final OperationalDayRepository operationalDayRepository;

    @Transactional(readOnly = true)
    public int countByDateBetween(LocalDate start, LocalDate end) {
        return operationalDayRepository.countByDateBetween(start, end);
    }

    @Transactional
    public void deleteByDateBetween(LocalDate start, LocalDate end) {
        int deletedRows = operationalDayRepository.deleteByDateBetween(start, end);
        log.debug("Удалено {} операционных дней", deletedRows);
    }

    @Transactional
    public void saveAll(List<OperationalDayEntity> operationalDays) {
        operationalDayRepository.saveAll(operationalDays);
        log.debug("Выполнена пакетная вставка операционных дней");
    }
}
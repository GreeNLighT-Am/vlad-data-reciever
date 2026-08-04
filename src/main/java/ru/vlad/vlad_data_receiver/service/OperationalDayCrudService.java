package ru.vlad.vlad_data_receiver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vlad.vlad_data_receiver.exceptions.OperationalDayNotFoundException;
import ru.vlad.vlad_data_receiver.repository.OperationalDayRepository;
import ru.vlad.vlad_data_receiver.repository.entity.OperationalDayEntity;

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

    @Transactional(readOnly = true)
    @Cacheable(value = "operationalDayCache", key = "#date", unless = "#result == null")
    public OperationalDayEntity findByDate(LocalDate date) {
        log.debug("Операционный день на {} не найден в кэше, делаем запрос к БД", date);
        return operationalDayRepository.findByDate(date).orElseThrow(() -> {
            String errorMessage = String.format("Не найден операционный день на %s", date);
            log.error(errorMessage);
            return new OperationalDayNotFoundException(errorMessage);
        });
    }
}
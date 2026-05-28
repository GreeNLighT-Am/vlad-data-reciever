package ru.vlad.vlad_data_receiver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vlad.vlad_data_receiver.entity.OperationalDay;
import ru.vlad.vlad_data_receiver.repository.OperationalDayRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CRUDService {

    private final OperationalDayRepository operationalDayRepository;

    public int countByDateBetween(LocalDate start, LocalDate end) {
        return operationalDayRepository.countByDateBetween(start, end);
    }

    public void deleteByDateBetween(LocalDate start, LocalDate end) {
        operationalDayRepository.deleteByDateBetween(start, end);
    }

    public void saveAll(List<OperationalDay> operationalDays) {
        operationalDayRepository.saveAll(operationalDays);
    }
}
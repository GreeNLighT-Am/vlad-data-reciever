package ru.vlad.vlad_data_receiver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vlad.vlad_data_receiver.entity.OperationalDayEntity;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationalDayService {

    private final OperationalDayCrudService crudService;

    @Transactional
    public void insertOperDays() {
        LocalDate firstDayOfNextMonth = LocalDate.now()
                .with(TemporalAdjusters.firstDayOfNextMonth());
        LocalDate lastDayOfNextMonth = firstDayOfNextMonth
                .with(TemporalAdjusters.lastDayOfMonth());

        int totalDaysInNextMonth = lastDayOfNextMonth.getDayOfMonth();
        int existingDaysCount = crudService.countByDateBetween(firstDayOfNextMonth, lastDayOfNextMonth);

        try {
            if (existingDaysCount == 0) {
                log.debug("Опердни на следующий месяц ({}) отсутствуют. Начинаем заполнение...", firstDayOfNextMonth.getMonth());
                fillOperationalDays(firstDayOfNextMonth, lastDayOfNextMonth);
            } else if (existingDaysCount == totalDaysInNextMonth) {
                log.warn("Внимание, генерация пропущена: все опердни на следующий месяц ({}) уже присутствуют в базе данных!", firstDayOfNextMonth.getMonth());
            } else {
                log.warn("Опердни на следующий месяц есть, но не на весь месяц (найдено {} из {}). Перезапускаем заполнение...",
                        existingDaysCount, totalDaysInNextMonth);

                crudService.deleteByDateBetween(firstDayOfNextMonth, lastDayOfNextMonth);

                fillOperationalDays(firstDayOfNextMonth, lastDayOfNextMonth);
            }
        } catch (RuntimeException e) {
            log.error("Ошибка выполнения функции заполнения опердней на следующий месяц: ", e);
        }
    }

    private void fillOperationalDays(LocalDate firstDayOfNextMonth, LocalDate lastDayOfNextMonth) {
        List<OperationalDayEntity> daysToInsert = new ArrayList<>();
        LocalDate loopDate = firstDayOfNextMonth;

        while (!loopDate.isAfter(lastDayOfNextMonth)) {
            OperationalDayEntity operationalDay = OperationalDayEntity.builder()
                    .date(loopDate)
                    .stateId(1L)
                    .build();
            daysToInsert.add(operationalDay);
            loopDate = loopDate.plusDays(1);
        }

        crudService.saveAll(daysToInsert);
        log.info("Успешно заполнены опердни на весь следующий месяц с {} по {}.", firstDayOfNextMonth, lastDayOfNextMonth);
    }
}
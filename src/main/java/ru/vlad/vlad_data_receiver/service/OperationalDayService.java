package ru.vlad.vlad_data_receiver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vlad.vlad_data_receiver.repository.OperationalDayRepository;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationalDayService {

    private final OperationalDayRepository repository;

    @Transactional
    public String insertOperDays() {
        log.info("Начало выполнения функции заполнения опердней на следующий месяц");
        long startTime = System.currentTimeMillis();

        LocalDate nextMonthDate = LocalDate.now().plusMonths(1);
        LocalDate startDay = nextMonthDate.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endDay = nextMonthDate.with(TemporalAdjusters.lastDayOfMonth());

        int totalDaysInNextMonth = nextMonthDate.lengthOfMonth();
        int existingDaysCount = repository.countDaysInPeriod(startDay, endDay);

        try {
            if (existingDaysCount == 0) {
                log.info("Опердни на следующий месяц ({}) отсутствуют. Начинаем заполнение...", nextMonthDate.getMonth());
                fillOperationalDays(startDay, endDay);
                return String.format("Успешно заполнены опердни на весь следующий месяц с %s по %s.", startDay, endDay);
            }
            if (existingDaysCount == totalDaysInNextMonth) {
                log.warn("Внимание: Все опердни на следующий месяц ({}) уже присутствуют в базе данных!", nextMonthDate.getMonth());
                return "Генерация пропущена: все дни уже существуют.";
            }
            log.warn("Опердни на следующий месяц есть, но не на весь месяц (найдено {} из {}). Перезапускаем заполнение...",
                    existingDaysCount, totalDaysInNextMonth);

            repository.deleteDaysInPeriod(startDay, endDay);

            fillOperationalDays(startDay, endDay);

            return String.format("Частичные опердни удалены. Заново заполнен весь следующий месяц с %s по %s.", startDay, endDay);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка выполнения функции заполнения опердней на следующий месяц: " + e.getMessage(), e);
        } finally {
            log.info("Обработка операционных дней выполнена за {} ms", System.currentTimeMillis() - startTime);
        }
    }

    private void fillOperationalDays(LocalDate start, LocalDate end) {
        List<LocalDate> datesToInsert = new ArrayList<>();
        LocalDate loopDate = start;

        while (!loopDate.isAfter(end)) {
            datesToInsert.add(loopDate);
            loopDate = loopDate.plusDays(1);
        }

        repository.saveAll(datesToInsert, 1);
        log.info("Выполнена пакетная вставка {} опердней.", datesToInsert.size());
    }
}
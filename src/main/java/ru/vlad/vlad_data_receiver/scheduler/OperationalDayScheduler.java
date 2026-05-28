package ru.vlad.vlad_data_receiver.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.vlad.vlad_data_receiver.service.OperationalDayService;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperationalDayScheduler {

    private final OperationalDayService operationalDayService;

    @Scheduled(cron = "${insert-operdays-scheduler}")
    public void generateOperationalDays() {
        long startTime = System.currentTimeMillis();
        log.info("Начало выполнения функции заполнения опердней на следующий месяц");
        operationalDayService.insertOperDays();
        log.info("Обработка операционных дней выполнена за {} ms", System.currentTimeMillis() - startTime);
    }
}
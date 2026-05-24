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
        log.info("=== ЗАПУСК OperationalDayScheduler ===");
        try {
            String result = operationalDayService.insertOperDays();
            log.info("Результат выполнения функции: {}", result);
        } catch (Exception e) {
            log.error("Ошибка в работе OperationalDayScheduler", e);
        } finally {
            log.info("=== ЗАВЕРШЕНИЕ OperationalDayScheduler ===");
        }
    }
}
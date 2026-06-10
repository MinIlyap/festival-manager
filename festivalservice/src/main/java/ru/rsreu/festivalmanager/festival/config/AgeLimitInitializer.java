package ru.rsreu.festivalmanager.festival.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.rsreu.festivalmanager.festival.entity.festival.AgeLimit;
import ru.rsreu.festivalmanager.festival.repository.AgeLimitRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgeLimitInitializer implements CommandLineRunner {
    private final AgeLimitRepository ageLimitRepository;

    @Override
    public void run(String... args) {
        List<String> defaultAgeLimits = List.of("6+", "12+", "16+", "18+");

        log.info("Проверка справочника возрастных ограничений (age_limits)...");

        int addedCount = 0;

        for (String limitValue : defaultAgeLimits) {
            if (!ageLimitRepository.existsByAgeLimitValue(limitValue)) {
                AgeLimit ageLimit = new AgeLimit();
                ageLimit.setAgeLimitValue(limitValue);
                ageLimitRepository.save(ageLimit);
                addedCount++;
                log.info("Добавлено возрастное ограничение: {}", limitValue);
            }
        }

        if (addedCount > 0) {
            log.info("Справочник age_limits успешно обновлен. Добавлено записей: {}", addedCount);
        }
    }
}

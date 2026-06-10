package ru.rsreu.festivalmanager.common.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public class CoreFeignConfig {
    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    public static class CustomErrorDecoder implements ErrorDecoder {
        private final ErrorDecoder defaultErrorDecoder = new Default();

        @Override
        public Exception decode(String methodKey, Response response) {
            String message = extractErrorMessage(response);
            log.error("Feign client error in method [{}], status [{}], message: {}", methodKey, response.status(), message);

            return switch (response.status()) {
                case 400 -> new IllegalArgumentException("Некорректный запрос к внешнему сервису: " + message);
                case 403 -> new RuntimeException("Нет доступа к внешнему сервису (403 Forbidden)");
                case 404 -> new IllegalArgumentException("Ресурс не найден во внешнем сервисе: " + message);
                case 500 -> new RuntimeException("Внутренняя ошибка внешнего сервиса");
                default -> defaultErrorDecoder.decode(methodKey, response);
            };
        }

        private String extractErrorMessage(Response response) {
            try {
                if (response.body() != null) {
                    try (InputStream bodyIs = response.body().asInputStream()) {
                        return new String(bodyIs.readAllBytes(), StandardCharsets.UTF_8);
                    }
                }
            } catch (Exception e) {
                log.warn("Не удалось прочитать тело ошибки Feign: {}", e.getMessage());
            }
            return "Без описания";
        }
    }
}

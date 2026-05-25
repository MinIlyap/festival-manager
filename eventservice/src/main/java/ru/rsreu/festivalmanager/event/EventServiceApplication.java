package ru.rsreu.festivalmanager.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import ru.rsreu.festivalmanager.common.config.CoreFeignConfig;
import ru.rsreu.festivalmanager.jwt.annotation.EnableFestivalSecurity;

@SpringBootApplication
@ComponentScan(basePackages = "ru.rsreu.festivalmanager")
@EnableFestivalSecurity
@EnableFeignClients(defaultConfiguration = CoreFeignConfig.class)
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class EventServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventServiceApplication.class, args);
    }
}

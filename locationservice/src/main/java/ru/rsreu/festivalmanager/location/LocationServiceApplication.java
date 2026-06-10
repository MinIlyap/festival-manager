package ru.rsreu.festivalmanager.location;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import ru.rsreu.festivalmanager.jwt.annotation.EnableFestivalSecurity;

@SpringBootApplication
@ComponentScan(basePackages = "ru.rsreu.festivalmanager")
@EnableFestivalSecurity
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class LocationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LocationServiceApplication.class, args);
    }
}

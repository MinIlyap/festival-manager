package ru.rsreu.festivalmanager.festival;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "ru.rsreu.festivalmanager")
public class FestivalServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FestivalServiceApplication.class, args);
    }

}

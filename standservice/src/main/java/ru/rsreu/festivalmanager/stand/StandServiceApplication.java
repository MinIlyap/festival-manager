package ru.rsreu.festivalmanager.stand;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "ru.rsreu.festivalmanager")
public class StandServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StandServiceApplication.class, args);
    }

}

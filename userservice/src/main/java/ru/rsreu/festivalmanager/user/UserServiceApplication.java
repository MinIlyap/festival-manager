package ru.rsreu.festivalmanager.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import ru.rsreu.festivalmanager.common.config.CoreFeignConfig;
import ru.rsreu.festivalmanager.jwt.annotation.EnableFestivalSecurity;

@SpringBootApplication
@ComponentScan(basePackages = "ru.rsreu.festivalmanager")
@EnableFeignClients(defaultConfiguration = CoreFeignConfig.class)
@EnableFestivalSecurity
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}

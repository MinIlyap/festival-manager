package ru.rsreu.festivalmanager.file;

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
public class FileServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileServiceApplication.class, args);
    }

}

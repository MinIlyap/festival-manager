package ru.rsreu.festivalmanager.jwt.annotation;

import org.springframework.context.annotation.ComponentScan;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ComponentScan(basePackages = "ru.rsreu.festivalmanager.jwt")
public @interface EnableFestivalSecurity {
}

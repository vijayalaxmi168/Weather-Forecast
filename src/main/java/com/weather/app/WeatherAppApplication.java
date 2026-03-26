package com.weather.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point.
 *
 * Django equivalent: manage.py runserver
 *
 * Run this app with:
 *   mvn spring-boot:run
 *   — OR —
 *   java -jar target/weather-app-1.0.0.jar
 */
@SpringBootApplication
public class WeatherAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherAppApplication.class, args);
    }
}

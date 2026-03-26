package com.weather.app;

import com.weather.app.exception.WeatherException;
import com.weather.app.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic smoke tests for WeatherService.
 *
 * Django equivalent: tests.py using TestCase
 *
 * Run with: mvn test
 */
@SpringBootTest
class WeatherServiceTest {

    @Autowired
    private WeatherService weatherService;

    @Test
    void contextLoads() {
        // Verifies the Spring context starts without errors
        assertNotNull(weatherService);
    }

    @Test
    void invalidCityThrowsWeatherException() {
        assertThrows(WeatherException.class, () ->
                weatherService.getWeather("xyznonexistentcity12345")
        );
    }
}

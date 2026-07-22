package com.weather.app;

import com.weather.app.exception.WeatherException;
import com.weather.app.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class WeatherServiceTest {

    @Autowired
    private WeatherService weatherService;

    @Test
    void contextLoads() {
      
        assertNotNull(weatherService);
    }

    @Test
    void invalidCityThrowsWeatherException() {
        assertThrows(WeatherException.class, () ->
                weatherService.getWeather("xyznonexistentcity12345")
        );
    }
}

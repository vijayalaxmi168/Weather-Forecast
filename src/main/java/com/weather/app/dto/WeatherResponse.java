package com.weather.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Top-level model sent to Thymeleaf.
 *
 * Django equivalent — what views.py put into context:
 *   return render(request, 'home.html', {'weather': weather_data})
 *
 * In Spring, WeatherController adds this object to Model:
 *   model.addAttribute("weather", weatherResponse);
 *
 * Then Thymeleaf reads it as: ${weather.city}, ${weather.current.temp}, etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse {

    private String city;     // "London"
    private String country;  // "GB"

    /** Current conditions — built from the first slot in the forecast list */
    private CurrentWeather current;

    /** 5-day grouped list — one entry per calendar day */
    private List<DailyForecast> forecast;

    // ─── Nested DTO for current conditions ──────────────────────────────
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentWeather {
        private int temp;
        private int feelsLike;
        private String description;
        private String icon;
        private int humidity;
        private double windSpeed;
    }
}

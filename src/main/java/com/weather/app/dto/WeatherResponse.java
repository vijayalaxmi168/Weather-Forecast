package com.weather.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse {
    // ✅ NEW — true when we're serving cached fallback data, not live API
    private boolean fallback = false;

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

package com.weather.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;



@Data                                        // Lombok: generates getters, setters, toString, equals, hashCode
@JsonIgnoreProperties(ignoreUnknown = true)  // ignore extra JSON fields
public class WeatherApiResponse {
    // ✅ NEW — true when we're serving cached fallback data, not live API
    private boolean fallback = false;

    @JsonProperty("city")
    private CityInfo city;

    @JsonProperty("list")
    private List<ForecastItem> forecastList;

    // ─── Nested: city block ───────────────────────────────────────────────
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CityInfo {

        @JsonProperty("name")
        private String name;

        @JsonProperty("country")
        private String country;
    }

    // ─── Nested: one 3-hour slot inside "list" ────────────────────────────
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ForecastItem {

        /** "2024-01-15 12:00:00" — same as forecast['dt_txt'] in Python */
        @JsonProperty("dt_txt")
        private String dtTxt;

        @JsonProperty("main")
        private MainData main;

        @JsonProperty("weather")
        private List<WeatherCondition> weather;

        @JsonProperty("wind")
        private WindData wind;

        // ── main block ────────────────────────────────────────────────────
        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class MainData {
            @JsonProperty("temp")
            private double temp;

            @JsonProperty("feels_like")
            private double feelsLike;

            @JsonProperty("humidity")
            private int humidity;
        }

        // ── weather[0] block ──────────────────────────────────────────────
        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class WeatherCondition {
            @JsonProperty("description")
            private String description;

            @JsonProperty("icon")
            private String icon;
        }

        // ── wind block ────────────────────────────────────────────────────
        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class WindData {
            @JsonProperty("speed")
            private double speed;
        }
    }
}

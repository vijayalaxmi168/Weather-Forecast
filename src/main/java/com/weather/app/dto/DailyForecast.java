package com.weather.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyForecast {

    private String date;           // "2024-01-15"
    private String dayName;        // "Monday"
    private String formattedDate;  // "15 Jan 2024"

    private int tempMax;           // °C
    private int tempMin;           // °C
    private int tempAvg;           // °C

    private String description;    // "Partly Cloudy"
    private String icon;           // "02d"  → used in img src

    private int humidity;          // %
    private double windSpeed;      // m/s
}

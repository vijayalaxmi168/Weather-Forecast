package com.weather.app.service;

import com.weather.app.dto.DailyForecast;
import com.weather.app.dto.WeatherApiResponse;
import com.weather.app.dto.WeatherApiResponse.ForecastItem;
import com.weather.app.dto.WeatherResponse;
import com.weather.app.dto.WeatherResponse.CurrentWeather;
import com.weather.app.exception.WeatherException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.cache.annotation.Cacheable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class WeatherService {

    // ─── Injected from application.properties ────────────────────────────
    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.base-url}")
    private String baseUrl;

    @Value("${weather.api.units}")
    private String units;

    // ─── HTTP client — equivalent to Python's `requests` library ─────────
    private final RestTemplate restTemplate;

    /**
     * Constructor injection — the recommended way in Spring Boot 3.
     * Spring automatically injects the RestTemplate bean from AppConfig.
     *
     * Django equivalent: nothing — Python just imports requests globally.
     */
    public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ─────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Fetches weather data for a city and returns a UI-ready WeatherResponse.
     *
     * Django equivalent: the entire get_weather(city) function in utils.py
     */
    @Cacheable(value = "weatherCache", key = "#city.toLowerCase()")
    public WeatherResponse getWeather(String city) {

        String url = buildUrl(city);
        log.debug("Calling OpenWeatherMap API: {}", url);

        WeatherApiResponse apiResponse = callApi(url);      // ① HTTP call
        return buildWeatherResponse(apiResponse);           // ② map → DTO
    }


    private String buildUrl(String city) {
        return String.format("%s?q=%s&appid=%s&units=%s", baseUrl, city, apiKey, units);
    }


    private WeatherApiResponse callApi(String url) {
        try {
            WeatherApiResponse response = restTemplate.getForObject(url, WeatherApiResponse.class);

            if (response == null) {
                throw new WeatherException("No data received from weather service.");
            }
            return response;

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("City not found for URL: {}", url);
            throw new WeatherException("City not found! Please check the city name and try again.");

        } catch (HttpClientErrorException e) {
            log.error("API client error: {}", e.getMessage());
            throw new WeatherException("Weather service error: " + e.getStatusCode());

        } catch (Exception e) {
            // ✅ NEW: Instead of throwing, load fallback data
            log.warn("OpenWeatherMap API failed. Switching to fallback data. Reason: {}", e.getMessage());
            return loadFallbackData();
        }
    }
    // ✅ NEW METHOD — loads fallback JSON from classpath
    private WeatherApiResponse loadFallbackData() {
        try {
            org.springframework.core.io.ClassPathResource resource =
                    new org.springframework.core.io.ClassPathResource("fallback-weather.json");

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            WeatherApiResponse fallback = mapper.readValue(resource.getInputStream(), WeatherApiResponse.class);
            fallback.setFallback(true); // ✅ flag so the UI can show the banner
            return fallback;

        } catch (Exception ex) {
            log.error("Could not load fallback weather data", ex);
            throw new WeatherException("Weather service is currently unavailable. Please try again later.");
        }
    }


    private WeatherResponse buildWeatherResponse(WeatherApiResponse apiResponse) {

        List<ForecastItem> forecastList = apiResponse.getForecastList();

        // ── Current weather from slot[0] ──────────────────────────────────
        // Django equivalent: current = forecasts[0]
        ForecastItem firstSlot = forecastList.get(0);
        CurrentWeather current = CurrentWeather.builder()
                .temp((int) Math.round(firstSlot.getMain().getTemp()))
                .feelsLike((int) Math.round(firstSlot.getMain().getFeelsLike()))
                .description(capitalize(firstSlot.getWeather().get(0).getDescription()))
                .icon(firstSlot.getWeather().get(0).getIcon())
                .humidity(firstSlot.getMain().getHumidity())
                .windSpeed(Math.round(firstSlot.getWind().getSpeed() * 10.0) / 10.0)
                .build();

        // ── Group 3-hour slots by calendar date ──────────────────────────
        // Django equivalent:
        //   daily = defaultdict(list)
        //   for item in forecasts:
        //       date_str = item['dt_txt'].split(' ')[0]
        //       daily[date_str].append(item)
        Map<String, List<ForecastItem>> byDay = forecastList.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getDtTxt().split(" ")[0],  // "2024-01-15 12:00:00" → "2024-01-15"
                        LinkedHashMap::new,                      // preserve insertion order
                        Collectors.toList()
                ));

        // ── Build one DailyForecast per date ─────────────────────────────
        List<DailyForecast> dailyForecasts = byDay.entrySet().stream()
                .map(entry -> buildDailyForecast(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        return WeatherResponse.builder()
                .city(apiResponse.getCity().getName())
                .country(apiResponse.getCity().getCountry())
                .current(current)
                .forecast(dailyForecasts)
                .fallback(apiResponse.isFallback())   // ✅ add this line
                .build();
    }

    /**
     * Summarises all 3-hour slots for one day into a single DailyForecast.
     *
     * Django equivalent:
     *   temps = [i['main']['temp'] for i in items]
     *   midday = next((i for i in items if '12:00:00' in i['dt_txt']), items[0])
     *   daily_summary.append({ 'temp_max': max(temps), ... })
     */
    private DailyForecast buildDailyForecast(String dateStr, List<ForecastItem> items) {

        // Collect temperatures
        DoubleSummaryStatistics tempStats = items.stream()
                .mapToDouble(i -> i.getMain().getTemp())
                .summaryStatistics();

        // Prefer midday slot for icon/description; fall back to first slot
        // Django equivalent: midday = next((i for i in items if '12:00:00' in i['dt_txt']), items[0])
        ForecastItem representative = items.stream()
                .filter(i -> i.getDtTxt().contains("12:00:00"))
                .findFirst()
                .orElse(items.get(0));

        // Average humidity & wind
        double avgHumidity = items.stream().mapToInt(i -> i.getMain().getHumidity()).average().orElse(0);
        double avgWind     = items.stream().mapToDouble(i -> i.getWind().getSpeed()).average().orElse(0);

        // Format the date for display
        // Django equivalent: date_obj.strftime('%A') and date_obj.strftime('%d %b %Y')
        LocalDate date          = LocalDate.parse(dateStr);
        String    dayName       = date.format(DateTimeFormatter.ofPattern("EEEE"));   // "Monday"
        String    formattedDate = date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")); // "15 Jan 2024"

        return DailyForecast.builder()
                .date(dateStr)
                .dayName(dayName)
                .formattedDate(formattedDate)
                .tempMax((int) Math.round(tempStats.getMax()))
                .tempMin((int) Math.round(tempStats.getMin()))
                .tempAvg((int) Math.round(tempStats.getAverage()))
                .description(capitalize(representative.getWeather().get(0).getDescription()))
                .icon(representative.getWeather().get(0).getIcon())
                .humidity((int) Math.round(avgHumidity))
                .windSpeed(Math.round(avgWind * 10.0) / 10.0)
                .build();
    }

    /** Title-cases a string. Django equivalent: .title() */
    private String capitalize(String str) {
        if (str == null || str.isBlank()) return str;
        return Arrays.stream(str.split(" "))
                .map(w -> w.isEmpty() ? w : Character.toUpperCase(w.charAt(0)) + w.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}

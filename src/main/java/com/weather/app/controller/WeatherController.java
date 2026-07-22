package com.weather.app.controller;

import com.weather.app.dto.WeatherResponse;
import com.weather.app.exception.WeatherException;
import com.weather.app.service.WeatherService;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**

 * @Controller  — tells Spring this class handles HTTP requests and returns view names
 *               (use @RestController only when returning JSON, not HTML pages)
 * @Slf4j       — Lombok injects: private static final Logger log = ...
 */
@Slf4j
@Controller
public class WeatherController {

    private final WeatherService weatherService;

   
    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/")
    public String home() {
        return "index";   // → resolves to src/main/resources/templates/index.html
    }

   
    @PostMapping("/weather")
    public String getWeather(
            @RequestParam @NotBlank String city,   // replaces request.POST.get('city')
            Model model                            // replaces the context dict {}
    ) {
        log.debug("Weather request received for city: {}", city);

        try {
            WeatherResponse weather = weatherService.getWeather(city.trim());

       
            model.addAttribute("weather", weather);
            model.addAttribute("searchedCity", city);
            model.addAttribute("isFallback", weather.isFallback()); // ✅ NEW

        } catch (WeatherException e) {
           
            log.warn("Weather fetch failed: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("searchedCity", city);
        }

        return "index";   
    }
}

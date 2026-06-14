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
 * ══════════════════════════════════════════════════════════════════
 * WeatherController  —  Java equivalent of Django views.py + urls.py
 * ══════════════════════════════════════════════════════════════════
 *
 * DJANGO → SPRING MAPPING
 * ┌─────────────────────────────────┬────────────────────────────────────┐
 * │ Django                          │ Spring                             │
 * ├─────────────────────────────────┼────────────────────────────────────┤
 * │ urls.py: path('', views.home)   │ @GetMapping("/")                   │
 * │ if request.method == 'POST':    │ @PostMapping("/weather")           │
 * │ request.POST.get('city')        │ @RequestParam String city          │
 * │ render(request, 'home.html', {})│ model.addAttribute(...); return "" │
 * │ {'weather': weather_data}       │ model.addAttribute("weather", ...)  │
 * │ {'error': '...'}               │ model.addAttribute("error", ...)   │
 * └─────────────────────────────────┴────────────────────────────────────┘
 *
 * @Controller  — tells Spring this class handles HTTP requests and returns view names
 *               (use @RestController only when returning JSON, not HTML pages)
 * @Slf4j       — Lombok injects: private static final Logger log = ...
 */
@Slf4j
@Controller
public class WeatherController {

    private final WeatherService weatherService;

    /**
     * Constructor injection — Spring auto-wires WeatherService here.
     * Django has no equivalent; it just imports functions.
     */
    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    // ─────────────────────────────────────────────────────────────────────
    // GET /  — Show empty home page
    // ─────────────────────────────────────────────────────────────────────
    /**
     * Django equivalent:
     *   def home(request):
     *       return render(request, 'weather_app/home.html')   # GET branch
     *
     * Returning "index" tells Thymeleaf to render: templates/index.html
     */
    @GetMapping("/")
    public String home() {
        return "index";   // → resolves to src/main/resources/templates/index.html
    }

    // ─────────────────────────────────────────────────────────────────────
    // POST /weather  — Handle form submission
    // ─────────────────────────────────────────────────────────────────────
    /**
     * Django equivalent:
     *   def home(request):
     *       if request.method == 'POST':
     *           city = request.POST.get('city').strip()
     *           weather_data = get_weather(city)
     *           if 'error' in weather_data:
     *               return render(request, 'home.html', {'error': weather_data['error']})
     *           return render(request, 'home.html', {'weather': weather_data})
     *
     * Key differences from Django:
     *  • GET and POST are separate methods (cleaner than if/else on request.method)
     *  • model.addAttribute() replaces the context dict
     *  • Exceptions replace error-dict returns
     *  • @RequestParam Spring automatically extracts the form field
     */
    @PostMapping("/weather")
    public String getWeather(
            @RequestParam @NotBlank String city,   // replaces request.POST.get('city')
            Model model                            // replaces the context dict {}
    ) {
        log.debug("Weather request received for city: {}", city);

        try {
            WeatherResponse weather = weatherService.getWeather(city.trim());

            // model.addAttribute("weather", weather)
            // → Thymeleaf: ${weather.city}, ${weather.current.temp}, etc.
            model.addAttribute("weather", weather);
            model.addAttribute("searchedCity", city);
            model.addAttribute("isFallback", weather.isFallback()); // ✅ NEW

        } catch (WeatherException e) {
            // model.addAttribute("error", "City not found!")
            // → Thymeleaf: ${error}
            log.warn("Weather fetch failed: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("searchedCity", city);
        }

        return "index";   // always render the same template (like Django's single view)
    }
}

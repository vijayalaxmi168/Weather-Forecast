package com.weather.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Spring configuration class — equivalent to Django's settings.py for beans.
 *
 * RestTemplate is Spring's HTTP client.
 * Django equivalent: the `requests` library you imported in utils.py.
 *
 *   Python:  response = requests.get(url)
 *   Java:    WeatherApiResponse r = restTemplate.getForObject(url, WeatherApiResponse.class);
 *
 * Declaring it as a @Bean means Spring manages one shared instance
 * and injects it wherever you write @Autowired / constructor injection.
 */
@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

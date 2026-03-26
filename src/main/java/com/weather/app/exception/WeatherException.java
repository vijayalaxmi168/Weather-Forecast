package com.weather.app.exception;

/**
 * Thrown by WeatherService when the API returns 404 or any error.
 *
 * Django equivalent — returning {'error': 'City not found!'} from utils.py.
 * In Java we throw exceptions instead of returning error dicts.
 *
 * The controller catches this and adds the message to the Thymeleaf model.
 */
public class WeatherException extends RuntimeException {

    public WeatherException(String message) {
        super(message);
    }
}

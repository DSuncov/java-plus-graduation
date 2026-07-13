package ru.practicum.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class ServiceErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String s, Response response) {
        String message = extractMessage(response);
        ErrorResponse errorResponse = tryParseErrorDto(message);

        return switch (response.status()) {
            case 400 -> new ValidationException(message);
            case 404 -> new NotFoundException(message);
            case 409 -> new ConflictException(message);
            case 500 -> new InternalServerException(message);

            default -> defaultErrorDecoder.decode(s, response);
        };
    }

    private ErrorResponse tryParseErrorDto(String message) {
        try {
            return objectMapper.readValue(message, ErrorResponse.class);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractMessage(Response response) {
        if (response.body() == null) {
            return "No response body";
        }
        try {
            return Util.toString(response.body().asReader(StandardCharsets.UTF_8));
        } catch (IOException e) {
            return "Error reading response";
        }
    }
}

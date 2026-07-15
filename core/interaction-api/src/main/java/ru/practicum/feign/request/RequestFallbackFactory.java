package ru.practicum.feign.request;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class RequestFallbackFactory implements FallbackFactory<AdminRequestClient> {

    @Override
    public AdminRequestClient create(Throwable cause) {
        if (cause instanceof RuntimeException) {
            return new RequestFallback();
        }

        return null;
    }
}

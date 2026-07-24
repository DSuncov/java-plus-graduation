package ru.practicum.feign.event;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class AdminEventFallbackFactory implements FallbackFactory<AdminEventClient> {

    @Override
    public AdminEventClient create(Throwable cause) {
        if (cause instanceof RuntimeException) {
            return new EventFallback();
        }

        return null;
    }
}

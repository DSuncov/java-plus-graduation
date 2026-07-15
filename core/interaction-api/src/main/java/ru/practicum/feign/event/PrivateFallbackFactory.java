package ru.practicum.feign.event;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class PrivateFallbackFactory implements FallbackFactory<PrivateEventClient> {

    @Override
    public PrivateEventClient create(Throwable cause) {
        if (cause instanceof RuntimeException) {
            return new EventFallback();
        }

        return null;
    }
}

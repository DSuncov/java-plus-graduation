package ru.practicum.feign.event;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "event-service",
        contextId = "privateEventClient",
        path = "/users",
        fallback = EventFallback.class,
        fallbackFactory = PrivateFallbackFactory.class
)
public interface PrivateEventClient extends PrivateEventOperations {
}

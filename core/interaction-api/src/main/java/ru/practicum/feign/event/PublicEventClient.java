package ru.practicum.feign.event;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "event-service",
        contextId = "publicEventClient",
        path = "/events",
        fallback = EventFallback.class,
        fallbackFactory = PublicFallbackFactory.class
)
public interface PublicEventClient extends PublicEventOperations {
}

package ru.practicum.feign.event;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "event-service",
        contextId = "adminEventClient",
        path = "/admin/events",
        fallback = EventFallback.class,
        fallbackFactory = AdminEventFallbackFactory.class
)
public interface AdminEventClient extends AdminEventOperations {
}

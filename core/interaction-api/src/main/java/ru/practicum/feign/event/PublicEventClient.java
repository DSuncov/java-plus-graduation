package ru.practicum.feign.event;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "event-service",
        contextId = "publicEventClient",
        path = "/events"
)
public interface PublicEventClient extends PublicEventOperations {
}

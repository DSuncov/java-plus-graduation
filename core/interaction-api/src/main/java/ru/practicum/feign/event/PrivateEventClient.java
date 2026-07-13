package ru.practicum.feign.event;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "event-service",
        contextId = "privateEventClient",
        path = "/users"
)
public interface PrivateEventClient extends PrivateEventOperations {
}

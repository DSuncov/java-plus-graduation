package ru.practicum.feign.request;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "request-service",
        contextId = "adminRequestClient",
        path = "/requests",
        fallback = RequestFallback.class,
        fallbackFactory = RequestFallbackFactory.class
)
public interface AdminRequestClient extends AdminRequestOperations {
}

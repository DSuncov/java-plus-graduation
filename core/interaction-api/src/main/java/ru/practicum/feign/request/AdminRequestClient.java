package ru.practicum.feign.request;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "request-service",
        contextId = "adminRequestClient",
        path = "/requests"
)
public interface AdminRequestClient extends AdminRequestOperations {
}

package ru.practicum.feign.user;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "user-service",
        contextId = "adminUserClient",
        path = "/admin/users",
        fallback = UseFallBack.class,
        fallbackFactory = UserClientFallbackFactory.class
)
public interface AdminUserClient extends AdminUserOperations {
}

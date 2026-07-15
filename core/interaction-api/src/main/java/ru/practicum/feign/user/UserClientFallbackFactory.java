package ru.practicum.feign.user;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class UserClientFallbackFactory implements FallbackFactory<AdminUserClient> {

    @Override
    public AdminUserClient create(Throwable cause) {
        if (cause instanceof RuntimeException) {
            return new UseFallBack();
        }

        return null;
    }
}

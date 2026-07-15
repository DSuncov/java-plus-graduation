package ru.practicum.feign.user;

import org.springframework.http.ResponseEntity;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserRequestDto;
import ru.practicum.dto.user.UserShortDto;

import java.util.List;

public class UseFallBack implements AdminUserClient{

    @Override
    public ResponseEntity<UserDto> create(UserRequestDto userRequestDto) {
        throw new RuntimeException("Сервис недоступен.");
    }

    @Override
    public ResponseEntity<List<UserDto>> getAllUsers(List<Long> ids, int from, int size) {
        throw new RuntimeException("Сервис недоступен.");
    }

    @Override
    public ResponseEntity<Void> deleteUser(Long id) {
        throw new RuntimeException("Сервис недоступен.");
    }

    @Override
    public UserShortDto getUserById(Long id) {
        throw new RuntimeException("Сервис недоступен.");
    }

    @Override
    public List<UserShortDto> getUsersByIds(List<Long> ids) {
        throw new RuntimeException("Сервис недоступен.");
    }
}

package ru.practicum.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserRequestDto;
import ru.practicum.dto.user.UserShortDto;

import java.util.List;

public interface UserAdminService {

    UserDto create(UserRequestDto userRequestDto);

    List<UserDto> getAllUsers(List<Long> ids, Pageable pageable);

    UserShortDto getUserById(Long id);

    void deleteUser(Long id);

    List<UserShortDto> getUsersByIds(List<Long> ids);
}

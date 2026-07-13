package ru.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserRequestDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.feign.user.AdminUserOperations;
import ru.practicum.service.UserAdminService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/users")
public class UserAdminController implements AdminUserOperations {

    private final UserAdminService userAdminService;

    @Override
    public ResponseEntity<UserDto> create(@Valid @RequestBody @NotNull UserRequestDto userRequestDto) {
        log.info("запрос на создание пользователя: UserController");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userAdminService.create(userRequestDto));
    }

    @Override
    public ResponseEntity<List<UserDto>> getAllUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        log.info("запрос на вывод всех пользователей: UserController");
        return ResponseEntity.ok(userAdminService.getAllUsers(ids, PageRequest.of(from / size, size)));
    }

    @Override
    public ResponseEntity<Void> deleteUser(@PathVariable @NotNull @Positive Long id) {
        log.info("запрос на удаление пользователя: UserController");
        userAdminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public UserShortDto getUserById(Long id) {
        log.info("запрос на получение информации о пользователе с id: {}", id);
        return userAdminService.getUserById(id);
    }

    @Override
    public List<UserShortDto> getUsersByIds(List<Long> ids) {
        return userAdminService.getUsersByIds(ids);
    }
}

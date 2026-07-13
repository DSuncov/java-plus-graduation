package ru.practicum.feign.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserRequestDto;
import ru.practicum.dto.user.UserShortDto;

import java.util.List;

public interface AdminUserOperations {

    @PostMapping
    ResponseEntity<UserDto> create(@Valid @RequestBody @NotNull UserRequestDto userRequestDto);

    @GetMapping
    ResponseEntity<List<UserDto>> getAllUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size);

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteUser(@PathVariable @NotNull @Positive Long id);

    @GetMapping("/{id}")
    UserShortDto getUserById(@PathVariable @NotNull @Positive Long id);

    @GetMapping("/short")
    List<UserShortDto> getUsersByIds(@RequestParam List<Long> ids);
}

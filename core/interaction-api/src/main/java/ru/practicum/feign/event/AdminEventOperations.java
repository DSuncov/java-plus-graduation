package ru.practicum.feign.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.PatchEventDto;

import java.util.List;

public interface AdminEventOperations {

    @GetMapping
    ResponseEntity<List<EventFullDto>> findEventsBy(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(required = false,  defaultValue = "0") Integer from,
            @RequestParam(required = false, defaultValue = "10") Integer size);

    @PatchMapping("/{eventId}")
    ResponseEntity<EventFullDto> patchEvent(
            @PathVariable("eventId") @NotNull @Positive Long eventId,
            @Valid @RequestBody @NotNull PatchEventDto patchEventDto);

    @GetMapping("/{id}")
    Boolean existEventById(@PathVariable @NotNull @Positive Long id);
}

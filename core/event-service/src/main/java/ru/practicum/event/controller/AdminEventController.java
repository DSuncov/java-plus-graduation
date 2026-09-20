package ru.practicum.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.PatchEventDto;
import ru.practicum.event.param.AdminEventParam;
import ru.practicum.event.service.EventService;
import ru.practicum.feign.event.AdminEventOperations;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
@Slf4j
public class AdminEventController implements AdminEventOperations {

    private final EventService eventService;

    @Override
    public ResponseEntity<List<EventFullDto>> findEventsBy(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(required = false,  defaultValue = "0") Integer from,
            @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        AdminEventParam param = new AdminEventParam(users, states, categories, rangeStart, rangeEnd, from, size);
        return ResponseEntity.ok(eventService.findEventsBy(param));
    }

    @Override
    public ResponseEntity<EventFullDto> patchEvent(
            @PathVariable("eventId") @NotNull @Positive Long id,
            @Valid @RequestBody @NotNull PatchEventDto patchEventDto) {
        return ResponseEntity.ok(eventService.patchEvent(id, patchEventDto));
    }

    @Override
    public Boolean existEventById(@PathVariable @NotNull @Positive Long id) {
        return eventService.existEventById(id);
    }
}

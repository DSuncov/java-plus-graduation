package ru.practicum.feign.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.PatchEventDto;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.List;

public interface PrivateEventOperations {

    @GetMapping("/{userId}/events")
    ResponseEntity<List<EventShortDto>> findEventsBy(
            @PathVariable("userId") @NotNull @Positive Long userId,
            @RequestParam(value = "from", defaultValue = "0") Integer from,
            @RequestParam(value = "size", defaultValue = "10") Integer size);

    @GetMapping("/{userId}/events/{eventId}")
    ResponseEntity<EventFullDto> findEventById(
            @PathVariable("userId") @NotNull @Positive Long userId,
            @PathVariable("eventId") @NotNull @Positive Long eventId);

    @PostMapping("/{userId}/events")
    ResponseEntity<EventFullDto> saveNewEvent(
            @PathVariable("userId") @NotNull @Positive Long userId,
            @Valid @RequestBody @NotNull NewEventDto newEventDto);

    @PatchMapping("/{userId}/events/{eventId}")
    ResponseEntity<EventFullDto> patchEventByInitiator(
            @PathVariable("userId") @NotNull @Positive Long userId,
            @PathVariable("eventId") @NotNull @Positive Long eventId,
            @Valid @RequestBody @NotNull PatchEventDto patchEventDto);

    @GetMapping("/{userId}/events/{eventId}/requests")
    ResponseEntity<List<ParticipationRequestDto>> findRequestsForEventsByUser(
            @PathVariable("userId") @NotNull @Positive Long userId,
            @PathVariable("eventId") @NotNull @Positive Long eventId);

    @PatchMapping("/{userId}/events/{eventId}/requests")
    ResponseEntity<EventRequestStatusUpdateResult> patchStatusOfRequest(
            @PathVariable("userId") @NotNull @Positive Long userId,
            @PathVariable("eventId") @NotNull @Positive Long eventId,
            @RequestBody @NotNull @Valid EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);
}

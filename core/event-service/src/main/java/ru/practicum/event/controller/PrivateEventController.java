package ru.practicum.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.PatchEventDto;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.event.service.EventService;
import ru.practicum.feign.event.PrivateEventOperations;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class PrivateEventController implements PrivateEventOperations {

    private final EventService eventService;

    @Override
    public ResponseEntity<List<EventShortDto>> findEventsBy(
            @PathVariable("userId") @NotNull @Positive Long userId,
            @RequestParam(value = "from", defaultValue = "0") Integer from,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return ResponseEntity.ok(eventService.findEventsBy(userId, from, size));
    }

    @Override
    public ResponseEntity<EventFullDto> findEventById(
            @PathVariable("userId") @NotNull @Positive Long userId,
            @PathVariable("eventId") @NotNull @Positive Long eventId) {
        return ResponseEntity.ok(eventService.findEventByIdAndUser(userId, eventId));
    }

    @Override
    public ResponseEntity<EventFullDto> saveNewEvent(
            @PathVariable("userId") @NotNull @Positive Long userId,
            @Valid @RequestBody @NotNull NewEventDto newEventDto) {
        EventFullDto eventFullDto = eventService.saveNewEvent(userId, newEventDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventFullDto);
    }

    @Override
    public ResponseEntity<EventFullDto> patchEventByInitiator(
            @PathVariable("userId") @NotNull @Positive Long userId,
            @PathVariable("eventId") @NotNull @Positive Long eventId,
            @Valid @RequestBody @NotNull PatchEventDto patchEventDto) {
        return ResponseEntity.ok(eventService.patchEventByUser(userId, eventId, patchEventDto));
    }

    @Override
    public ResponseEntity<List<ParticipationRequestDto>> findRequestsForEventsByUser(
            @PathVariable("userId") @NotNull @Positive Long userId,
            @PathVariable("eventId") @NotNull @Positive Long eventId) {
        return ResponseEntity.ok(eventService.getEventParticipants(userId, eventId));
    }

    @Override
    public ResponseEntity<EventRequestStatusUpdateResult> patchStatusOfRequest(
            @PathVariable("userId") @NotNull @Positive Long userId,
            @PathVariable("eventId") @NotNull @Positive Long eventId,
            @RequestBody @NotNull @Valid EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        return ResponseEntity.ok(eventService.changeRequestStatus(userId, eventId, eventRequestStatusUpdateRequest));
    }
}

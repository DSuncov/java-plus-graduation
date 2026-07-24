package ru.practicum.feign.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.List;

public interface AdminRequestOperations {

    @GetMapping("/{eventId}/count")
    Long countConfirmedRequestsByEventId(
            @PathVariable("eventId") @NotNull @Positive Long eventId);

    @PostMapping("/count/confirm")
    List<Object[]> countConfirmedRequestsForEvents(
            @RequestParam("events") List<Long> events);

    @GetMapping("/users/{userId}/events/{eventId}")
    List<ParticipationRequestDto> findRequestsForEventsByUser(
            @PathVariable("userId") @NotNull @Positive Long userId,
            @PathVariable("eventId") @NotNull @Positive Long eventId);

    @PatchMapping("/users/{userId}/events/{eventId}")
    EventRequestStatusUpdateResult patchStatusOfRequest(
            @PathVariable("userId") @NotNull @Positive Long userId,
            @PathVariable("eventId") @NotNull @Positive Long eventId,
            @RequestBody @NotNull @Valid EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);

    @GetMapping("/users/{userId}/events/{eventId}/confirm")
    boolean isUserConformedRequest(
            @PathVariable("userId") @NotNull @Positive Long userId,
            @PathVariable("eventId") @NotNull @Positive Long eventId);
}

package ru.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.feign.request.AdminRequestOperations;
import ru.practicum.service.RequestService;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/requests")
public class AdminRequestController implements AdminRequestOperations {

    private final RequestService requestService;

    @Override
    public Long countConfirmedRequestsByEventId(
            @PathVariable("eventId") @NotNull @Positive Long eventId) {
        return requestService.countConfirmedRequestsByEventId(eventId);
    }

    @Override
    public List<Object[]> countConfirmedRequestsForEvents(
            @RequestParam("events") List<Long> events) {
        return requestService.countConfirmedRequestsForEvents(events);
    }

    @Override
    public List<ParticipationRequestDto> findRequestsForEventsByUser(
            @PathVariable("userId") @NotNull @Positive Long userId,
            @PathVariable("eventId") @NotNull @Positive Long eventId) {
        return requestService.getEventParticipants(userId, eventId);
    }

    @Override
    public EventRequestStatusUpdateResult patchStatusOfRequest(
            @PathVariable("userId") @NotNull @Positive Long userId,
            @PathVariable("eventId") @NotNull @Positive Long eventId,
            @RequestBody @NotNull @Valid EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        return requestService.changeRequestStatus(userId, eventId, eventRequestStatusUpdateRequest);
    }

    @Override
    public boolean isUserConformedRequest(
            @PathVariable("userId") @NotNull @Positive Long userId,
            @PathVariable("eventId") @NotNull @Positive Long eventId) {
        return requestService.isUserConformedRequest(userId, eventId);
    }
}

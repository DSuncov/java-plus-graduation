package ru.practicum.feign.request;

import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.List;

public class RequestFallback implements AdminRequestClient {
    @Override
    public Long countConfirmedRequestsByEventId(Long eventId) {
        throw new RuntimeException("Сервис недоступен.");
    }

    @Override
    public List<Object[]> countConfirmedRequestsForEvents(List<Long> events) {
        throw new RuntimeException("Сервис недоступен.");
    }

    @Override
    public List<ParticipationRequestDto> findRequestsForEventsByUser(Long userId, Long eventId) {
        throw new RuntimeException("Сервис недоступен.");
    }

    @Override
    public EventRequestStatusUpdateResult patchStatusOfRequest(Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        throw new RuntimeException("Сервис недоступен.");
    }

    @Override
    public boolean isUserConformedRequest(Long userId, Long eventId) {
        throw new RuntimeException("Сервис недоступен.");
    }
}

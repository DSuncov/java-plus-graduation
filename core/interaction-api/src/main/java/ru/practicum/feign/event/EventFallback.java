package ru.practicum.feign.event;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.PatchEventDto;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.List;

public class EventFallback implements AdminEventClient, PrivateEventClient, PublicEventClient {

    @Override
    public ResponseEntity<List<EventFullDto>> findEventsBy(List<Long> users, List<String> states, List<Long> categories, String rangeStart, String rangeEnd, Integer from, Integer size) {
        throw new RuntimeException("Сервис недоступен.");
    }

    @Override
    public ResponseEntity<EventFullDto> patchEvent(Long eventId, PatchEventDto patchEventDto) {
        throw new RuntimeException("Сервис недоступен.");
    }

    @Override
    public ResponseEntity<List<EventShortDto>> findEventsBy(Long userId, Integer from, Integer size) {
        throw new RuntimeException("Сервис недоступен.");
    }

    @Override
    public ResponseEntity<EventFullDto> findEventById(Long userId, Long eventId) {
        throw new RuntimeException("Сервис недоступен.");
    }

    @Override
    public ResponseEntity<EventFullDto> saveNewEvent(Long userId, NewEventDto newEventDto) {
        throw new RuntimeException("Сервис недоступен.");
    }

    @Override
    public ResponseEntity<EventFullDto> patchEventByInitiator(Long userId, Long eventId, PatchEventDto patchEventDto) {
        throw new RuntimeException("Сервис недоступен.");
    }

    @Override
    public ResponseEntity<List<ParticipationRequestDto>> findRequestsForEventsByUser(Long userId, Long eventId) {
        throw new RuntimeException("Сервис недоступен.");
    }

    @Override
    public ResponseEntity<EventRequestStatusUpdateResult> patchStatusOfRequest(Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        throw new RuntimeException("Сервис недоступен.");
    }

    @Override
    public ResponseEntity<List<EventShortDto>> findEventsBy(String text, List<Long> categories, Boolean paid, String rangeStart, String rangeEnd, Boolean onlyAvailable, String sort, Integer from, Integer size, HttpServletRequest httpServletRequest) {
        throw new RuntimeException("Сервис недоступен.");
    }

    @Override
    public ResponseEntity<EventFullDto> findEventById(Long id, HttpServletRequest httpServletRequest) {
        throw new RuntimeException("Сервис недоступен.");
    }
}

package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.PatchEventDto;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.event.param.AdminEventParam;
import ru.practicum.event.param.PublicEventParam;

import java.util.List;

public interface EventService {

    List<EventShortDto> findEventsBy(PublicEventParam param, HttpServletRequest httpServletRequest);

    List<EventFullDto> findEventsBy(AdminEventParam param);

    List<EventShortDto> findEventsBy(Long id, Integer from, Integer size);

    EventFullDto findEventById(Long id, HttpServletRequest httpServletRequest);

    EventFullDto patchEvent(Long id, PatchEventDto patchEventDto);

    EventFullDto patchEventByUser(Long userId, Long eventId, PatchEventDto patchEventDto);

    EventFullDto findEventByIdAndUser(Long userId, Long eventId);

    EventFullDto saveNewEvent(Long userId, NewEventDto newEventDto);

    List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId);

    EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);
}

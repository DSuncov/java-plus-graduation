package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.PatchEventDto;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.enums.SortType;
import ru.practicum.enums.State;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;
import ru.practicum.event.param.AdminEventParam;
import ru.practicum.event.param.PublicEventParam;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.spec.AdminEventSpecification;
import ru.practicum.event.spec.EventSpecification;
import ru.practicum.event.spec.PublicEventSpecification;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.feign.request.AdminRequestClient;
import ru.practicum.feign.user.AdminUserClient;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final AdminUserClient userClient;
    private final CategoryRepository categoryRepository;
    private final AdminRequestClient requestClient;
    private final EventMapper eventMapper;
    private final StatsClient statsClient;

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> findEventsBy(PublicEventParam param, HttpServletRequest httpServletRequest) {
        saveHit(httpServletRequest);

        EventSpecification specification = PublicEventSpecification.builder()
                .text(param.getText())
                .categories(param.getCategories())
                .paid(param.getPaid())
                .onlyAvailable(param.getOnlyAvailable())
                .rangeStart(param.getRangeStart())
                .rangeEnd(param.getRangeEnd())
                .build();

        Pageable pageable = PageRequest.of(param.getFrom(), param.getSize());

        if (param.getSort() != null && param.getSort().isBlank()) {
            if (String.valueOf(SortType.EVENT_DATE).equals(param.getSort())) {
                Sort sort = Sort.by(Sort.Direction.DESC, param.getSort());
                pageable = PageRequest.of(param.getFrom(), param.getSize(), sort);
                Page<Event> events = eventRepository.findAll(specification.toSpecification(), pageable);

                Map<Event, UserShortDto> userShortDtos = getUsersShortDto(events.getContent());

                return events.stream()
                        .map(e -> eventMapper.toShortDto(e, userShortDtos.get(e)))
                        .toList();
            }
        }

        List<Event> events = eventRepository.findAll(specification.toSpecification(), pageable).getContent();

        Map<Long, Long> viewsForEvents = getViews(events);
        Map<Long, Long> requestsForEvents = getRequestsForEvents(events);
        System.out.println(requestsForEvents.toString());

        List<EventShortDto> eventsDto = eventMapper.toListShortDtoWithViewsAndRequests(events, viewsForEvents, requestsForEvents, getUsersShortDto(events));

        if (param.getSort() != null && !param.getSort().isBlank()) {
            if (String.valueOf(SortType.VIEWS).equals(param.getSort())) {
                eventsDto.sort(Comparator.comparing(EventShortDto::getViews).reversed());
            }
        }

        return eventsDto;
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventFullDto> findEventsBy(AdminEventParam param) {
        EventSpecification specification = AdminEventSpecification.builder()
                .users(param.getUsers())
                .states(param.getStates())
                .categories(param.getCategories())
                .rangeStart(param.getRangeStart())
                .rangeEnd(param.getRangeEnd())
                .build();

        Pageable pageable = PageRequest.of(param.getFrom(), param.getSize());

        List<Event> events = eventRepository.findAll(specification.toSpecification(), pageable).getContent();

        Map<Long, Long> viewsForEvents = getViews(events);
        Map<Long, Long> requestsForEvents = getRequestsForEvents(events);

        return eventMapper.toListFullDtoWithViewsAndRequests(events, viewsForEvents, requestsForEvents, getUsersShortDto(events));
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto findEventById(Long id, HttpServletRequest httpServletRequest) {
        log.info("Получение пользователя по id.");
        Event event = eventRepository.findPublishedEventById(id)
                .orElseThrow(() -> new NotFoundException(String.format("События с id = %d не существует.", id)));
        log.info("Информация о событии получена.");
        saveHit(httpServletRequest);

        UserShortDto userShortDto = userClient.getUserById(event.getInitiatorId());

        EventFullDto eventFullDto = eventMapper.toFullDto(event, userShortDto);

        log.info("Получаем количество просмотров.");
        eventFullDto.setViews(getStats(event));
        eventFullDto.setConfirmedRequests(requestClient.countConfirmedRequestsByEventId(id));
        return eventFullDto;
    }

    @Transactional
    @Override
    public EventFullDto patchEvent(Long id, PatchEventDto patchEventDto) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id = %d отсутствует.", id)));

        if (!event.getState().equals(State.PENDING)) {
            throw new ConflictException("Событие можно публиковать, только если оно в состоянии ожидания публикации.");
        }

        if (patchEventDto.getStateAction() != null) {
            switch (patchEventDto.getStateAction()) {
                case "PUBLISH_EVENT" -> {
                    event.setState(State.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    eventRepository.save(event);
                }

                case "REJECT_EVENT" -> {
                    event.setState(State.CANCELED);
                    eventRepository.save(event);
                }
            }
        }

        UserShortDto userShortDto = userClient.getUserById(event.getInitiatorId());

        patchFieldValidation(event, patchEventDto);
        EventFullDto eventFullDto = eventMapper.toFullDto(event, userShortDto);

        if (event.getPublishedOn() != null) {
            eventFullDto.setViews(getStats(event));

            if (event.getEventDate().plusHours(1).isBefore(event.getPublishedOn())) {
                throw new ConflictException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации. " +
                        "Дата события: " + event.getEventDate() + ", дата публикации: " + event.getPublishedOn());
            }
        }

        eventFullDto.setConfirmedRequests(requestClient.countConfirmedRequestsByEventId(id));

        return eventFullDto;
    }

    @Transactional
    @Override
    public EventFullDto patchEventByUser(Long userId, Long eventId, PatchEventDto patchEventDto) {
        if (userClient.getUserById(userId) == null) {
            throw new NotFoundException(String.format("Пользователя с id = %d не существует.", userId));
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id = %d отсутствует.", eventId)));

        if (!(event.getState().equals(State.PENDING) || event.getState().equals(State.CANCELED))) {
            throw new ConflictException("События со статусом PUBLISHED не могут быть изменены.");
        }

        if (patchEventDto.getStateAction() != null) {
            switch (patchEventDto.getStateAction()) {
                case "CANCEL_REVIEW" -> {
                    event.setState(State.CANCELED);
                    eventRepository.save(event);
                }

                case "SEND_TO_REVIEW" -> {
                    event.setState(State.PENDING);
                    eventRepository.save(event);
                }
            }
        }

        UserShortDto userShortDto = userClient.getUserById(event.getInitiatorId());

        EventFullDto eventFullDto = eventMapper.toFullDto(event, userShortDto);

        if (event.getPublishedOn() != null) {
            eventFullDto.setViews(getStats(event));
        }

        return eventFullDto;
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> findEventsBy(Long userId, Integer from, Integer size) {
        if (userClient.getUserById(userId) == null) {
            throw new NotFoundException(String.format("Пользователя с id = %d не существует.", userId));
        }

        Pageable pageable = PageRequest.of(from, size);
        List<Event> events = eventRepository.findAll(pageable).getContent();

        Map<Long, Long> viewsForEvents = getViews(events);
        Map<Long, Long> requestsForEvents = getRequestsForEvents(events);

        return eventMapper.toListShortDtoWithViewsAndRequests(events, viewsForEvents, requestsForEvents, getUsersShortDto(events));
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto findEventByIdAndUser(Long userId, Long eventId) {
        if (userClient.getUserById(userId) == null) {
            throw new NotFoundException(String.format("Пользователя с id = %d не существует.", userId));
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id = %d отсутствует.", eventId)));

        UserShortDto userShortDto = userClient.getUserById(event.getInitiatorId());

        EventFullDto eventFullDto = eventMapper.toFullDto(event, userShortDto);

        if (event.getPublishedOn() != null) {
            eventFullDto.setViews(getStats(event));
        }

        return eventFullDto;
    }

    @Transactional
    @Override
    public EventFullDto saveNewEvent(Long userId, NewEventDto newEventDto) {
        UserShortDto user = userClient.getUserById(userId);

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException(String.format("Категории с id = %d не существует.", newEventDto.getCategory())));

        Event event = eventMapper.toEntity(newEventDto, userId, category);
        Event createdEvent = eventRepository.save(event);

        UserShortDto userShortDto = userClient.getUserById(event.getInitiatorId());

        return eventMapper.toFullDto(createdEvent, userShortDto);
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {
        log.info("Получение запросов на участие в событии {} для пользователя {}", eventId, userId);

        if (userClient.getUserById(userId) == null) {
            throw new NotFoundException(String.format("Пользователь с id = %d не найден", userId));
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id = %d не найдено", eventId)));

        if (!event.getInitiatorId().equals(userId)) {
            throw new NotFoundException(String.format("Событие с id = %d не принадлежит пользователю %d", eventId, userId));
        }

        return requestClient.findRequestsForEventsByUser(userId, eventId);
    }

    @Override
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        log.info("Изменение статуса заявок для события {} пользователем {}", eventId, userId);

        if (userClient.getUserById(userId) == null) {
            throw new NotFoundException(String.format("Пользователь с id = %d не найден", userId));
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id = %d не найдено", eventId)));

        if (!event.getInitiatorId().equals(userId)) {
            throw new NotFoundException(String.format("Событие с id = %d не принадлежит пользователю %d", eventId, userId));
        }

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Нельзя изменять статус заявок для неопубликованного события");
        }

        return requestClient.patchStatusOfRequest(userId, eventId, eventRequestStatusUpdateRequest);
    }

    private Map<Long, Long> getRequestsForEvents(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> eventsIds = events.stream().map(Event::getId).toList();

        List<Object[]> results = requestClient.countConfirmedRequestsForEvents(eventsIds);

        return results.stream()
                .collect(Collectors.toMap(
                        o -> ((Number) o[0]).longValue(),
                        o -> ((Number) o[1]).longValue()
                ));
    }

    private void saveHit(HttpServletRequest request) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try {
            statsClient.saveHit(new EndpointHitDto(
                    null,
                    "main-service",
                    request.getRequestURI(),
                    request.getRemoteAddr(),
                    LocalDateTime.now().format(formatter)
            ));
        } catch (Exception e) {
            log.warn("Произошла ошибка при сохранении статистики.");
            throw new RuntimeException(e);
        }
    }

    private Long getStats(Event event) {
        try {
            List<ViewStatsDto> stats = statsClient.getStats(
                    event.getPublishedOn(),
                    LocalDateTime.now(),
                    List.of("/events/" + event.getId()),
                    true);

            return stats.isEmpty() ? 0L : stats.getFirst().getHits();
        } catch (Exception e) {
            log.warn("Произошла ошибка при получении статистики.");
            throw new RuntimeException(e);
        }
    }

    private Map<Long, Long> getViews(List<Event> events) {
        List<String> uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .toList();

        List<ViewStatsDto> stats = statsClient.getStats(
                LocalDateTime.now(),
                LocalDateTime.now(),
                uris,
                true);

        Pattern pattern = Pattern.compile("/events/(\\d+)");
        return stats.stream().collect(Collectors.toMap(s ->
               Long.parseLong(String.valueOf(pattern.matcher(s.getUri()).find())), ViewStatsDto::getHits));
    }

    private void patchFieldValidation(Event event, PatchEventDto patchEventDto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (patchEventDto.getCategory() != null) {
            Category category = categoryRepository.findById(patchEventDto.getCategory())
                    .orElseThrow(() -> new NotFoundException(String.format("Категории с id = %d не существует.", patchEventDto.getCategory())));
            event.setCategory(category);
        }

        if (patchEventDto.getLocation() != null) {
            Location location = event.getLocation();
            location.setLat(patchEventDto.getLocation().getLat());
            location.setLon(patchEventDto.getLocation().getLon());
            event.setLocation(location);
        }

        if (patchEventDto.getAnnotation() != null) {
            event.setAnnotation(patchEventDto.getAnnotation());
        }

        if (patchEventDto.getDescription() != null) {
            event.setDescription(patchEventDto.getDescription());
        }

        if (patchEventDto.getEventDate() != null) {
            event.setEventDate(LocalDateTime.parse(patchEventDto.getEventDate(), formatter));
        }

        if (patchEventDto.getPaid() != null) {
            event.setPaid(patchEventDto.getPaid());
        }

        if (patchEventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(patchEventDto.getParticipantLimit());
        }

        if (patchEventDto.getRequestModeration() != null) {
            event.setRequestModeration(patchEventDto.getRequestModeration());
        }

        if (patchEventDto.getTitle() != null) {
            event.setTitle(patchEventDto.getTitle());
        }
    }

    private Map<Event, UserShortDto> getUsersShortDto(List<Event> events) {
        List<Long> usersIds = events.stream()
                .map(Event::getInitiatorId)
                .toList();

        List<UserShortDto> result = userClient.getUsersByIds(usersIds);

        Map<Long, UserShortDto> usersByIds = result.stream()
                .collect(Collectors.toMap(UserShortDto::getId, Function.identity()));

        return events.stream()
                .filter(e -> usersByIds.containsKey(e.getInitiatorId()))
                .collect(Collectors.toMap(
                       Function.identity(),
                       e -> usersByIds.get(e.getInitiatorId())
                ));
    }
}

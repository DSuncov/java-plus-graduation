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
import ru.practicum.AnalyzerClient;
import ru.practicum.CollectorClient;
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
import ru.practicum.exception.ValidationException;
import ru.practicum.feign.request.AdminRequestClient;
import ru.practicum.feign.user.AdminUserClient;
import ru.practicum.grpc.stats.collector.ActionTypeProto;
import ru.practicum.grpc.stats.event.RecommendedEventProto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
    private final CollectorClient collectorClient;
    private final AnalyzerClient analyzerClient;

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> findEventsBy(PublicEventParam param, HttpServletRequest httpServletRequest) {
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

        Map<Long, Double> viewsForEvents = getRatingsForEvents(events);
        Map<Long, Long> requestsForEvents = getRequestsForEvents(events);
        System.out.println(requestsForEvents.toString());

        List<EventShortDto> eventsDto = eventMapper.toListShortDtoWithViewsAndRequests(events, viewsForEvents, requestsForEvents, getUsersShortDto(events));

        if (param.getSort() != null && !param.getSort().isBlank()) {
            if (String.valueOf(SortType.VIEWS).equals(param.getSort())) {
                eventsDto.sort(Comparator.comparing(EventShortDto::getRating).reversed());
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

        Map<Long, Double> viewsForEvents = getRatingsForEvents(events);
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
        collectorClient.sendUserAction(event.getInitiatorId(), id, ActionTypeProto.ACTION_VIEW);

        UserShortDto userShortDto = userClient.getUserById(event.getInitiatorId());

        EventFullDto eventFullDto = eventMapper.toFullDto(event, userShortDto);

        log.info("Получаем количество просмотров.");
        eventFullDto.setRating(getRatingForEvents(event));
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
            eventFullDto.setRating(getRatingForEvents(event));

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
            eventFullDto.setRating(getRatingForEvents(event));
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

        Map<Long, Double> viewsForEvents = getRatingsForEvents(events);
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
            eventFullDto.setRating(getRatingForEvents(event));
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

    @Override
    public Boolean existEventById(Long id) {
        return eventRepository.existsById(id);
    }

    @Override
    public List<EventShortDto> getRecommendations(Long userId, int maxResults) {
        return analyzerClient.getRecommendationsForUser(userId, maxResults)
                .map(proto -> getEventShortDto(proto.getEventId(), proto.getScore()))
                .toList();
    }

    private EventShortDto getEventShortDto(Long eventId, Double rating) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id = %d не найдено", eventId)));

        UserShortDto userShortDto = userClient.getUserById(event.getInitiatorId());
        if (userShortDto == null) {
            throw new NotFoundException("Пользователь с id = " + event.getInitiatorId() + " не найден");
        }

        EventShortDto eventShortDto = eventMapper.toShortDto(event, userShortDto);
        eventShortDto.setRating(rating);
        eventShortDto.setConfirmedRequests(0L);
        return eventShortDto;
    }

    @Override
    public void likeToEvent(Long eventId, Long userId) {
        if (eventRepository.existsById(eventId)) {
            throw new NotFoundException(String.format("Мероприятия с id = %d не найдено", eventId));
        }

        if (requestClient.isUserConformedRequest(eventId, userId)) {
            throw new ValidationException("Регистрация пользователя в мероприятии не принята.");
        }

        collectorClient.sendUserAction(userId, eventId, ActionTypeProto.ACTION_VIEW);
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

    private Double getRatingForEvents(Event event) {
        try {
            return analyzerClient.getInteractionsCount(List.of(event.getId()))
                    .findFirst()
                    .map(RecommendedEventProto::getScore)
                    .orElse(0.0);
        } catch (Exception e) {
            log.warn("Ошибка при получении рейтинга для требуемого события.");
            throw new RuntimeException(e);
        }
    }

    private Map<Long, Double> getRatingsForEvents(List<Event> events) {
        List<Long> eventsIds = events.stream().map(Event::getId).toList();

        try {
            return analyzerClient.getInteractionsCount(eventsIds)
                    .collect(Collectors.toMap(
                            RecommendedEventProto::getEventId,
                            RecommendedEventProto::getScore,
                            (a, b) -> a
                    ));
        } catch (Exception e) {
            log.warn("Ошибка при получении рейтинга для списка событий.");
            throw new RuntimeException(e);
        }
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

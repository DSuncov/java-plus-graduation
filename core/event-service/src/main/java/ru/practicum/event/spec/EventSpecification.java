package ru.practicum.event.spec;

import org.springframework.data.jpa.domain.Specification;

public interface EventSpecification {
    Specification<ru.practicum.event.model.Event> toSpecification();
}

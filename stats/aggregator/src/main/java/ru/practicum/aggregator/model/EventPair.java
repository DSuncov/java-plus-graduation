package ru.practicum.aggregator.model;

public record EventPair(long firstEvent, long secondEvent) {

    public static EventPair of(long firstEvent, long secondEvent) {
        if (firstEvent == secondEvent) {
            throw new IllegalArgumentException("Создать пару с самим собой невозможно.");
        }

        return new EventPair(Math.min(firstEvent, secondEvent), Math.max(firstEvent, secondEvent));
    }
}

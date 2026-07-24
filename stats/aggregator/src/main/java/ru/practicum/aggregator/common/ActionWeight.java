package ru.practicum.aggregator.common;

import ru.practicum.ewm.stats.avro.ActionTypeAvro;

public class ActionWeight {

    private ActionWeight() {}

    public static double getWeightFromAvro(ActionTypeAvro typeAvro) {
        return switch (typeAvro) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }
}

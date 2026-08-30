package ru.practicum.collector.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.stats.collector.ActionTypeProto;
import ru.practicum.grpc.stats.collector.UserActionProto;

import java.time.Instant;

@Component
public class UserActionMapper {

    public UserActionAvro mapToAvro(UserActionProto request) {
        return UserActionAvro.newBuilder()
                .setUserId(request.getUserId())
                .setEventId(request.getEventId())
                .setActionType(selectionType(request))
                .setTimestamp(Instant.ofEpochSecond(
                        request.getTimestamp().getSeconds(),
                        request.getTimestamp().getNanos()
                ))
                .build();
    }

    private ActionTypeAvro selectionType(UserActionProto request) {
        return switch (request.getActionType()) {
            case ActionTypeProto.ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ActionTypeProto.ACTION_LIKE -> ActionTypeAvro.LIKE;
            case ActionTypeProto.ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case UNRECOGNIZED-> throw new IllegalArgumentException("Неизвестный Action Type");
        };
    }
}

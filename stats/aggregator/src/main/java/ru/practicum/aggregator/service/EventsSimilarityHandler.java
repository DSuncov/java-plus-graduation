package ru.practicum.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.aggregator.common.ActionWeight;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.aggregator.model.EventPair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Component
@Slf4j
public class EventsSimilarityHandler {

    private final Map<Long, Map<Long, Double>> matrixWeights = new ConcurrentHashMap<>();
    private final Map<Long, Double> sumWeightByEvent = new ConcurrentHashMap<>();
    private final Map<Long, Map<Long, Double>>  minSumForPairOfEvents = new ConcurrentHashMap<>();

    public List<EventSimilarityAvro> calculateSimilarity(UserActionAvro action) {
        long eventId = action.getEventId();
        long userId = action.getUserId();
        double newWeight = ActionWeight.getWeightFromAvro(action.getActionType());

        Map<Long, Double> weightsByUser = matrixWeights.computeIfAbsent(eventId, key -> new ConcurrentHashMap<>());
        double oldWeight = weightsByUser.getOrDefault(userId, 0.0);

        if (newWeight <= oldWeight) {
            return List.of();
        }

        newWeight = newWeight * newWeight;
        weightsByUser.put(userId, newWeight);
        sumWeightByEvent.merge(eventId, newWeight - oldWeight, Double::sum);

        Set<EventPair> affectedPairs = new HashSet<>();
        for (Map.Entry<Long, Map<Long, Double>> entry : matrixWeights.entrySet()) {
            long otherEventId = entry.getKey();
            if (eventId == otherEventId) {
                log.info("Рассчитать сходство события с самим собой невозможно.");
                continue;
            }

            Double otherWeight = entry.getValue().get(userId);
            if (otherWeight == null) {
                log.info("Вес не задан.");
                continue;
            }

            EventPair eventPair = EventPair.of(eventId, otherEventId);

            double minDelta= Math.min(newWeight, otherWeight) - Math.min(oldWeight, otherWeight);

            minSumForPairOfEvents
                    .computeIfAbsent(eventPair.firstEvent(), key -> new ConcurrentHashMap<>())
                    .merge(eventPair.secondEvent(), minDelta, Double::sum);

            affectedPairs.add(eventPair);
        }

        List<EventSimilarityAvro> similarities = new ArrayList<>();
        for (EventPair pair : affectedPairs) {

            log.info("Выполняем расчет консинусного сходства двух событий.");
            double first = sumWeightByEvent.getOrDefault(pair.firstEvent(), 0.0);
            double second = sumWeightByEvent.getOrDefault(pair.secondEvent(), 0.0);
            double denominator = Math.sqrt(first * second);

            if (denominator == 0) {
                continue;
            }

            double minSum = minSumForPairOfEvents.getOrDefault(pair.firstEvent(), Map.of())
                    .getOrDefault(pair.secondEvent(), 0.0);

            log.info("Выполняем маппинг в авро для передачи в kafka.");
            EventSimilarityAvro eventSimilarityAvro = EventSimilarityAvro.newBuilder()
                    .setEventA(pair.firstEvent())
                    .setEventB(pair.secondEvent())
                    .setScore(minSum / denominator)
                    .setTimestamp(action.getTimestamp())
                    .build();

            similarities.add(eventSimilarityAvro);

            similarities.sort(Comparator.comparingLong(EventSimilarityAvro::getEventA)
                    .thenComparingLong(EventSimilarityAvro::getEventB));
        }

        return similarities;
    }
}

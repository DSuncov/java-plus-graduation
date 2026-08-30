package ru.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.grpc.stats.event.RecommendedEventProto;
import ru.practicum.analyzer.model.Interaction;
import ru.practicum.analyzer.model.Similarity;
import ru.practicum.analyzer.repository.InteractionsRepository;
import ru.practicum.analyzer.repository.SimilaritiesRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationService {

    private final InteractionsRepository interactionsRepository;
    private final SimilaritiesRepository similaritiesRepository;

    private static final int MAX_SIMILAR_COUNTS = 10;

    public List<RecommendedEventProto> getRecommendationsForUser(long userId, long maxResults) {
        List<Interaction> recentInteractions = interactionsRepository.findInteractionsByUserOrderByTimestampDesc(userId);
        if (recentInteractions.isEmpty()){
            return Collections.emptyList();
        }

        List<Long> interactedEventIds = recentInteractions.stream()
                .map(Interaction::getEventId)
                .toList();

        Map<Long, Double> candidateScores = new HashMap<>();
        for (Interaction interaction : recentInteractions.stream().limit(MAX_SIMILAR_COUNTS).toList()) {
            List<Similarity> similarities = similaritiesRepository.findSimilaritiesByEventId(interaction.getEventId());
            for (Similarity s : similarities) {
                long candidate = s.getFirstEvent().equals(interaction.getEventId()) ? s.getSecondEvent() : s.getFirstEvent();
                if (!interactedEventIds.contains(candidate)) {
                    double predictedRating = predictRating(userId, candidate);
                    candidateScores.put(candidate, predictedRating);
                }
            }
        }

        return candidateScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(e -> RecommendedEventProto.newBuilder()
                        .setEventId(e.getKey())
                        .setScore(e.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    public List<RecommendedEventProto> getSimilarEvents(long eventId, long userId, long maxResults) {
        List<Long> interactionsNotByUser = interactionsRepository.findEventsNotInteractionsWithUser(userId);
        List<Similarity> similarityList = similaritiesRepository.findSimilaritiesByEventId(eventId);

        return similarityList.stream()
                .map(s -> {
                    long similarEvent = s.getFirstEvent().equals(eventId) ? s.getSecondEvent() : s.getFirstEvent();
                    return new AbstractMap.SimpleEntry<>(similarEvent, s.getSimilarity());
                })
                .filter(entry -> interactionsNotByUser.contains(entry.getKey()))
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(entry -> RecommendedEventProto.newBuilder()
                        .setEventId(entry.getKey())
                        .setScore(entry.getValue())
                        .build())
                .toList();

    }

    public List<RecommendedEventProto> getInteractionsCount(List<Long> eventIdList) {
        return eventIdList.stream()
                .map(eventId -> {
                    double sumWeight = interactionsRepository.sumWeightByEventId(eventId);
                    return RecommendedEventProto.newBuilder()
                            .setEventId(eventId)
                            .setScore(sumWeight)
                            .build();
                })
                .toList();
    }

    private double predictRating(long eventId, long userId) {
        List<Interaction> userInteractions = interactionsRepository.findInteractionsByUserOrderByTimestampDesc(userId);
        Map<Long, Double> interactedRatings = userInteractions.stream()
                .collect(Collectors.toMap(Interaction::getEventId, Interaction::getRating));

        List<Similarity> similarities = similaritiesRepository.findSimilaritiesByEventId(eventId);
        List<Similarity> sortedSimilarities = similarities.stream()
                .sorted(Comparator.comparingDouble(Similarity::getSimilarity).reversed())
                .filter(s -> interactedRatings.containsKey(
                        s.getFirstEvent().equals(eventId) ? s.getSecondEvent() : s.getFirstEvent()))
                .limit(MAX_SIMILAR_COUNTS)
                .toList();

        double weightedSum = 0.0;
        double sumSimilarity = 0.0;
        for (Similarity s : sortedSimilarities) {
            long otherEvent = s.getFirstEvent().equals(eventId) ? s.getSecondEvent() : s.getFirstEvent();
            Double rating = interactedRatings.get(otherEvent);
            if (rating != null) {
                weightedSum += s.getSimilarity() * rating;
                sumSimilarity += s.getSimilarity();
            }
        }
        return sumSimilarity > 0 ? weightedSum / sumSimilarity : 0.0;
    }
}

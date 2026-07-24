package ru.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.analyzer.model.Interaction;

import java.util.List;

public interface InteractionsRepository extends JpaRepository<Interaction, Long> {

    @Query("SELECT i FROM Interaction i WHERE i.userId = :userId ORDER BY timestamp DESC")
    List<Interaction> findInteractionsByUserOrderByTimestampDesc(long userId);

    @Query("SELECT SUM(i.rating) FROM Interaction i WHERE i.eventId = :eventId")
    Double sumWeightByEventId(long eventId);

    @Query("SELECT i.eventId FROM Interaction i WHERE i.userId = :userId ORDER BY timestamp DESC LIMIT :maxSimilarCounts")
    List<Interaction> findEventsByUserOrderByTimestampDescWithLimit(long userId, int maxSimilarCounts);

    @Query("SELECT i.eventId from Interaction i WHERE i.userId != :userId")
    List<Long> findEventsNotInteractionsWithUser(long userId);

    @Query("SELECT i from Interaction i WHERE i.userId = :userId AND i.eventId = :eventId")
    Interaction findByUserIdAndEventId(long userId, Long eventId);
}

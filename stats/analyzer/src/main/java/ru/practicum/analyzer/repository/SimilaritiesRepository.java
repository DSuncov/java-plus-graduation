package ru.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.analyzer.model.Similarity;

import java.time.Instant;
import java.util.List;

public interface SimilaritiesRepository extends JpaRepository<Similarity, Long> {

    @Query("SELECT s FROM Similarity s WHERE s.firstEvent = :eventId OR s.secondEvent = :eventId")
    List<Similarity> findSimilaritiesByEventId(long eventId);

    @Modifying
    @Query("UPDATE Similarity s SET s.similarity = :similarity, s.timestamp = :timestamp WHERE s.firstEvent = :firstEvent AND s.secondEvent = :secondEvent")
    int updateSimilarity(@Param("firstEvent") Long firstEvent, @Param("secondEvent") Long secondEvent,
                         @Param("similarity") Float similarity, @Param("timestamp") Instant timestamp);
}

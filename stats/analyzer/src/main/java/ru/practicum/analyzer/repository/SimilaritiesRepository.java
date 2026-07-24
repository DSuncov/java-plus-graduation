package ru.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.analyzer.model.Similarity;

import java.util.List;

public interface SimilaritiesRepository extends JpaRepository<Similarity, Long> {

    @Query("SELECT s FROM Similarity s WHERE s.firstEvent = :eventId OR s.secondEvent = :eventId")
    List<Similarity> findSimilaritiesByEventId(long eventId);

//    @Query("SELECT s FROM Similarity s WHERE s.")
//    List<Similarity> findByEventId(long eventId);
}

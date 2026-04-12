package com.example.bossbot.stampanswer.repository;

import com.example.bossbot.stampanswer.entity.StampAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StampAnswerRepository extends JpaRepository<StampAnswer, Long> {

    /**
     * Find all active stamp answers ordered by created date descending
     */
    List<StampAnswer> findByIsActiveTrueOrderByCreatedAtDesc();

    /**
     * Search stamp answers by question or keywords (case-insensitive)
     */
    @Query("SELECT sa FROM StampAnswer sa WHERE sa.isActive = true " +
           "AND (LOWER(sa.question) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(sa.keywords) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<StampAnswer> searchByQuestionOrKeywords(@Param("searchTerm") String searchTerm);

    /**
     * Find top N most used stamp answers
     */
    List<StampAnswer> findTop10ByIsActiveTrueOrderByUsageCountDesc();

    /**
     * Check if question already exists (for unique validation)
     */
    boolean existsByQuestionIgnoreCase(String question);

    /**
     * Find by ID and active status
     */
    Optional<StampAnswer> findByIdAndIsActiveTrue(Long id);

    /**
     * Find active stamp answer by exact question (case-insensitive)
     */
    Optional<StampAnswer> findByQuestionIgnoreCaseAndIsActiveTrue(String question);
}

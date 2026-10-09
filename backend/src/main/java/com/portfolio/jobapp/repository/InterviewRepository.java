package com.portfolio.jobapp.repository;

import com.portfolio.jobapp.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByApplicationIdAndApplicationUserIdOrderByScheduledAtAsc(Long applicationId, Long userId);

    Optional<Interview> findByIdAndApplicationUserId(Long id, Long userId);

    long countByApplicationId(Long applicationId);

    void deleteByApplicationId(Long applicationId);
}


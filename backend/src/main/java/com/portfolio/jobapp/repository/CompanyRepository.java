package com.portfolio.jobapp.repository;

import com.portfolio.jobapp.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    List<Company> findByUserIdOrderByNameAsc(Long userId);

    Optional<Company> findByIdAndUserId(Long id, Long userId);

    boolean existsByNameIgnoreCaseAndUserId(String name, Long userId);

    boolean existsByNameIgnoreCaseAndUserIdAndIdNot(String name, Long userId, Long id);

    @Query("SELECT COUNT(a) FROM JobApplication a WHERE a.company.id = :companyId")
    long countApplicationsByCompanyId(@Param("companyId") Long companyId);
}


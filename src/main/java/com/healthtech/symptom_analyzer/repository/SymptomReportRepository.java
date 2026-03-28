package com.healthtech.symptom_analyzer.repository;

import com.healthtech.symptom_analyzer.model.SymptomReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SymptomReportRepository extends JpaRepository<SymptomReport, Long> {
}
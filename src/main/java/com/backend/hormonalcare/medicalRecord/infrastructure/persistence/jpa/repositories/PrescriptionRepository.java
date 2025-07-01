package com.backend.hormonalcare.medicalRecord.infrastructure.persistence.jpa.repositories;

import com.backend.hormonalcare.medicalRecord.domain.model.entities.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByMedicalRecordId(Long medicalRecordId);
}
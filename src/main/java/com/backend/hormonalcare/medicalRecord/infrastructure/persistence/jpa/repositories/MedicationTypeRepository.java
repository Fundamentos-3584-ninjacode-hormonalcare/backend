package com.backend.hormonalcare.medicalRecord.infrastructure.persistence.jpa.repositories;

import com.backend.hormonalcare.medicalRecord.domain.model.entities.MedicationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicationTypeRepository extends JpaRepository<MedicationType, Long> {

}


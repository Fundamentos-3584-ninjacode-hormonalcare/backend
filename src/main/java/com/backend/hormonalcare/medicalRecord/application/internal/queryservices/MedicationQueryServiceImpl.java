package com.backend.hormonalcare.medicalRecord.application.internal.queryservices;

import com.backend.hormonalcare.medicalRecord.domain.model.aggregates.Medication;
import com.backend.hormonalcare.medicalRecord.domain.model.queries.*;
import com.backend.hormonalcare.medicalRecord.domain.services.MedicationQueryService;
import com.backend.hormonalcare.medicalRecord.infrastructure.persistence.jpa.repositories.MedicationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedicationQueryServiceImpl implements MedicationQueryService {
    private final MedicationRepository medicationRepository;

    public MedicationQueryServiceImpl(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    @Override
    public List<Medication> handle(GetAllMedicationsQuery query) {
        return medicationRepository.findAll();
    }

    @Override
    public Optional<Medication> handle(GetMedicationByIdQuery query) {
        return medicationRepository.findById(query.id());
    }

 @Override
 public Optional<Medication> handle(GetMedicationsByPrescriptionIdQuery query) {
     return medicationRepository.findByPrescriptionId(query.prescriptionId());
 }

 @Override
 public Optional<Medication> handle(GetMedicationsByTypeIdQuery query) {
     return medicationRepository.findByMedicationTypeId(query.medicationTypeId());
 }

    @Override
    public Optional<Medication> handle(GetMedicationsByMedicalRecordIdQuery query) {
        return medicationRepository.findByMedicalRecordId(query.medicalRecordId());
    }

    @Override
    public Optional<Medication> handle(GetMedicationByDrugNameQuery query) {
        return medicationRepository.findByDrugName(query.drugName());
    }
}
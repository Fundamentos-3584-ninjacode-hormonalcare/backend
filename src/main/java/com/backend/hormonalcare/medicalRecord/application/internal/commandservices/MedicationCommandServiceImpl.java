package com.backend.hormonalcare.medicalRecord.application.internal.commandservices;

import com.backend.hormonalcare.medicalRecord.domain.model.aggregates.MedicalRecord;
import com.backend.hormonalcare.medicalRecord.domain.model.aggregates.Medication;
import com.backend.hormonalcare.medicalRecord.domain.model.commands.CreateMedicationCommand;
import com.backend.hormonalcare.medicalRecord.domain.model.commands.UpdateMedicationCommand;
import com.backend.hormonalcare.medicalRecord.domain.model.entities.MedicationType;
import com.backend.hormonalcare.medicalRecord.domain.model.entities.Prescription;
import com.backend.hormonalcare.medicalRecord.domain.services.MedicationCommandService;
import com.backend.hormonalcare.medicalRecord.infrastructure.persistence.jpa.repositories.MedicalRecordRepository;
import com.backend.hormonalcare.medicalRecord.infrastructure.persistence.jpa.repositories.MedicationRepository;
import com.backend.hormonalcare.medicalRecord.infrastructure.persistence.jpa.repositories.MedicationTypeRepository;
import com.backend.hormonalcare.medicalRecord.infrastructure.persistence.jpa.repositories.PrescriptionRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class MedicationCommandServiceImpl implements MedicationCommandService {
    private final MedicationRepository medicationRepository;
    private final MedicationTypeRepository medicationTypeRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    public MedicationCommandServiceImpl(MedicationRepository medicationRepository, MedicationTypeRepository medicationTypeRepository, PrescriptionRepository prescriptionRepository, MedicalRecordRepository medicalRecordRepository) {
        this.medicationRepository = medicationRepository;
        this.medicationTypeRepository = medicationTypeRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.medicalRecordRepository = medicalRecordRepository;
    }

    @Override
    public Optional<Medication> handle(CreateMedicationCommand command) {
        Prescription prescription = prescriptionRepository.findById(command.prescriptionId()).orElseThrow(() -> new RuntimeException("Prescription not found"));
        MedicationType medicationType = medicationTypeRepository.findById(command.medicalTypeId()).orElseThrow(() -> new RuntimeException("MedicationType not found"));
        MedicalRecord medicalRecord = medicalRecordRepository.findById(command.medicalRecordId()).orElseThrow(() -> new RuntimeException("MedicalRecord not found"));
        var medication = new Medication(command, medicalRecord,prescription, medicationType);
        medicationRepository.save(medication);
        return Optional.of(medication);
    }


    @Override
    public Optional<Medication> handle(UpdateMedicationCommand command) {
        var medication = medicationRepository.findById(command.id());

        if (medication.isEmpty()) {
            return Optional.empty();
        }

        var prescription = prescriptionRepository.findById(command.prescriptionId());
        var medicationType = medicationTypeRepository.findById(command.medicationTypeId());
        var medicalRecord = medicalRecordRepository.findById(command.medicalRecordId());

        if (prescription.isEmpty() || medicationType.isEmpty() || medicalRecord.isEmpty()  ){
            return Optional.empty();
        }

        medication.get().update(command,medicalRecord.get(), prescription.get(), medicationType.get());

        medicationRepository.save(medication.get());
        return Optional.of(medication.get());
    }
}
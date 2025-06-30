package com.backend.hormonalcare.medicalRecord.application.internal.queryservices;

import com.backend.hormonalcare.medicalRecord.application.internal.outboundservices.acl.ExternalProfileService;
import com.backend.hormonalcare.medicalRecord.domain.model.aggregates.Patient;
import com.backend.hormonalcare.medicalRecord.domain.model.queries.*;
import com.backend.hormonalcare.medicalRecord.domain.services.PatientQueryService;
import com.backend.hormonalcare.medicalRecord.infrastructure.persistence.jpa.repositories.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class PatientQueryServiceImpl implements PatientQueryService {
    private final PatientRepository patientRepository;
    private final ExternalProfileService externalProfileService;


    public PatientQueryServiceImpl(PatientRepository patientRepository, ExternalProfileService externalProfileService) {
        this.patientRepository = patientRepository;
        this.externalProfileService = externalProfileService;
    }
    @Override
    public Optional<Patient> handle(GetPatientByIdQuery query) {

        return patientRepository.findById(query.id());
    }

    @Override
    public Optional<Patient> handle(GetPatientByProfileIdQuery query) {
        return patientRepository.findByProfileId(query.profileId());
    }

    @Override
    public Optional<Patient> handle(GetPatientByPatientRecordIdQuery query) {
        return patientRepository.findByPatientRecordId(query.patientRecordId());
    }

    @Override
    public Optional<Long> handle(GetProfileIdByPatientIdQuery query) {
        return patientRepository.findById(query.patientId())
                .map(Patient::getProfileId);
    }

    @Override
    public List<Patient> handle(GetAllPatientsByDoctorIdQuery query) {
        return patientRepository.findByDoctor(query.doctorId());
    }

    @Override
    public List<Patient> handle(GetAllPatientsQuery query) {
        var patients = patientRepository.findAll();
        patients.forEach(patient -> {
            var profileDetailsOptional = externalProfileService.fetchProfileDetails(patient.getProfileId());
            profileDetailsOptional.ifPresent(profileDetails -> {
                System.out.println("Profile Details for Patient ID " + patient.getId() + ": " + profileDetails.getFullName());
            });
        });
        return patients;
    }
}



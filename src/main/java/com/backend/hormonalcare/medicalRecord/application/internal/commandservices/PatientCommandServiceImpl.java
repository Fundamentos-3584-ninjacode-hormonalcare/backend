package com.backend.hormonalcare.medicalRecord.application.internal.commandservices;

import com.backend.hormonalcare.medicalRecord.application.internal.outboundservices.acl.ExternalProfileService;
import com.backend.hormonalcare.medicalRecord.domain.events.PatientCreatedEvent;
import com.backend.hormonalcare.medicalRecord.domain.model.aggregates.Patient;
import com.backend.hormonalcare.medicalRecord.domain.model.commands.*;
import com.backend.hormonalcare.medicalRecord.domain.model.valueobjects.FamilyHistory;
import com.backend.hormonalcare.medicalRecord.domain.model.valueobjects.PersonalHistory;
import com.backend.hormonalcare.medicalRecord.domain.model.valueobjects.ProfileId;
import com.backend.hormonalcare.medicalRecord.domain.services.PatientCommandService;
import com.backend.hormonalcare.medicalRecord.infrastructure.persistence.jpa.repositories.DoctorRepository;
import com.backend.hormonalcare.medicalRecord.infrastructure.persistence.jpa.repositories.PatientRepository;
import com.backend.hormonalcare.shared.infrastructure.events.AsyncEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PatientCommandServiceImpl implements PatientCommandService {
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ExternalProfileService externalProfileService;
    private final AsyncEventPublisher asyncEventPublisher;

    public PatientCommandServiceImpl(PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            ExternalProfileService externalProfileService,
            AsyncEventPublisher asyncEventPublisher) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.externalProfileService = externalProfileService;
        this.asyncEventPublisher = asyncEventPublisher;
    }

    @Override
    public Optional<Patient> handle(CreatePatientCommand command) {

        Long doctorId = command.doctorId();
        if (doctorId == null || doctorId == 0) {
            doctorId = null;
        } else if (!doctorRepository.existsById(doctorId)) {
            throw new IllegalArgumentException("Doctor with id " + doctorId + " does not exist");
        }
        var profileId = externalProfileService.fetchProfileIdByPhoneNumber(command.phoneNumber());
        if (profileId.isEmpty()) {
            profileId = externalProfileService.createProfile(
                    command.firstName(),
                    command.lastName(),
                    command.gender(),
                    command.phoneNumber(),
                    command.image(),
                    command.birthday(),
                    command.userId());
        } else {
            patientRepository.findByProfileId(profileId.get()).ifPresent(patient -> {
                throw new IllegalArgumentException("Patient already exists");
            });
            doctorRepository.findByProfileId(profileId.get()).ifPresent(doctor -> {
                throw new IllegalArgumentException("Doctor already exists");
            });

        }
        if (profileId.isEmpty())
            throw new IllegalArgumentException("Unable to create profile");

        var patient = new Patient(profileId.get(), command.typeOfBlood(),
                new PersonalHistory(command.personalHistory()), new FamilyHistory(command.familyHistory()), doctorId);
        patientRepository.save(patient);
        // Usar publicación asíncrona de eventos
        asyncEventPublisher.publishEvent(new PatientCreatedEvent(patient.getId()));
        return Optional.of(patient);
    }

    @Override
    public Optional<Patient> handle(UpdatePatientCommand command) {
        var patientOptional = patientRepository.findById(command.id());
        if (patientOptional.isEmpty()) {
            throw new IllegalArgumentException("Patient with id " + command.id() + " does not exist");
        }

        var patient = patientOptional.get();

        boolean profileUpdated = externalProfileService.updateProfile(
                patient.getProfileId(),
                command.firstName(),
                command.lastName(),
                command.gender(),
                command.phoneNumber(),
                command.image(),
                command.birthday());
        if (!profileUpdated) {
            throw new IllegalArgumentException("Failed to update profile for patient with id " + command.id());
        }
        patient.updatePatient(command, new ProfileId(patient.getProfileId()));
        patientRepository.save(patient);
        return Optional.of(patient);
    }

    @Override
    public Optional<Patient> handle(UpdatePatientDoctorIdCommand command) {
        var id = command.id();
        if (!patientRepository.existsById(id)) {
            throw new IllegalArgumentException("Patient with id " + command.id() + "does not exists");
        }
        var result = patientRepository.findById(id);
        var patientToUpdate = result.get();
        try {
            Long doctorId = command.doctorId();
            if (doctorId == null || doctorId == 0) {
                doctorId = null;
            } else if (!doctorRepository.existsById(doctorId)) {
                throw new IllegalArgumentException("Doctor with id " + doctorId + " does not exist");
            }
            var updatedPatient = patientRepository.save(patientToUpdate.updateDoctorId(doctorId));
            return Optional.of(updatedPatient);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while updating patient: " + e.getMessage());
        }
    }

    @Override
    public Optional<Patient> handle(UpdatePersonalHistoryPatientCommand command) {
        var id = command.id();
        if (!patientRepository.existsById(id)) {
            throw new IllegalArgumentException("Patient with id " + command.id() + "does not exists");
        }
        var result = patientRepository.findById(id);
        var patientToUpdate = result.get();
        try {
            var updatedPatient = patientRepository.save(patientToUpdate.updatePersonalHistory(command));
            return Optional.of(updatedPatient);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while updating patient: " + e.getMessage());
        }
    }

    @Override
    public Optional<Patient> handle(UpdateFamilyHistoryPatientCommand command) {
        var id = command.id();
        if (!patientRepository.existsById(id)) {
            throw new IllegalArgumentException("Patient with id " + command.id() + "does not exists");
        }
        var result = patientRepository.findById(id);
        var patientToUpdate = result.get();
        try {
            var updatedPatient = patientRepository.save(patientToUpdate.updateFamilyHistory(command));
            return Optional.of(updatedPatient);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while updating patient: " + e.getMessage());
        }
    }

}

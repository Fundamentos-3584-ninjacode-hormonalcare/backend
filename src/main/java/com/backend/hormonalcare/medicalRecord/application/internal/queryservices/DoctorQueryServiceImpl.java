package com.backend.hormonalcare.medicalRecord.application.internal.queryservices;

import com.backend.hormonalcare.medicalRecord.domain.model.aggregates.Doctor;
import com.backend.hormonalcare.medicalRecord.domain.model.queries.*;
import com.backend.hormonalcare.medicalRecord.domain.services.DoctorQueryService;
import com.backend.hormonalcare.medicalRecord.infrastructure.persistence.jpa.repositories.DoctorRepository;
import com.backend.hormonalcare.medicalRecord.application.internal.outboundservices.acl.ExternalProfileService;
import com.backend.hormonalcare.medicalRecord.domain.model.valueobjects.ProfileId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorQueryServiceImpl implements DoctorQueryService {
    private final DoctorRepository doctorRepository;
    private final ExternalProfileService externalProfileService;

    public DoctorQueryServiceImpl(DoctorRepository doctorRepository, ExternalProfileService externalProfileService) {
        this.doctorRepository = doctorRepository;
        this.externalProfileService = externalProfileService;
    }

    @Override
    public Optional<Doctor> handle(GetDoctorByIdQuery query) {
        return doctorRepository.findById(query.id());
    }

    @Override
    public Optional<Doctor> handle(GetDoctorByProfileIdQuery query) {
        return doctorRepository.findByProfileId(query.profileId());
    }

    @Override
    public Optional<Doctor> handle(GetDoctorByDoctorRecordIdQuery query) {
        return doctorRepository.findByDoctorRecordId(query.doctorRecordId());
    }

    @Override
    public Optional<Long> handle(GetProfileIdByDoctorIdQuery query) {
        return doctorRepository.findById(query.doctorId())
                .map(Doctor::getProfileId) ;
    }

    @Override
    public List<Doctor> handle(GetAllDoctorsQuery query) {
        var doctors = doctorRepository.findAll();
        doctors.forEach(doctor -> {
            var profileDetailsOptional = externalProfileService.fetchProfileDetails(doctor.getProfileId());
            profileDetailsOptional.ifPresent(profileDetails -> {
                // Opcional: Log o procesamiento adicional
                System.out.println("Profile Details for Doctor ID " + doctor.getId() + ": " + profileDetails.getFullName());
            });
        });
        return doctors;
    }

    @Override
    public Optional<Doctor> handle(GetDoctorByUserId query) {
        Long userId = query.userId();
        return findDoctorByUserId(userId);
    }

    @Override
    public Optional<Doctor> findDoctorByUserId(Long userId) {
        Optional<Long> profileIdOpt = externalProfileService.fetchProfileIdByUserId(userId);
        if (profileIdOpt.isEmpty()) return Optional.empty();
        return doctorRepository.findByProfileId(new ProfileId(profileIdOpt.get()));
    }

}

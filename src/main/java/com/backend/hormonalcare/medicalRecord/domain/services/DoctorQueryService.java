package com.backend.hormonalcare.medicalRecord.domain.services;

import com.backend.hormonalcare.medicalRecord.domain.model.aggregates.Doctor;
import com.backend.hormonalcare.medicalRecord.domain.model.queries.*;

import java.util.List;
import java.util.Optional;

public interface DoctorQueryService {
    Optional<Doctor> handle(GetDoctorByIdQuery query);
    Optional<Doctor> handle(GetDoctorByProfileIdQuery query);
    Optional<Doctor> handle(GetDoctorByDoctorRecordIdQuery query);
    Optional<Long> handle(GetProfileIdByDoctorIdQuery query);
    List<Doctor> handle(GetAllDoctorsQuery query);

    Optional<Doctor> handle(GetDoctorByUserId query);

    Optional<Doctor> findDoctorByUserId(Long userId);
}

package com.backend.hormonalcare.medicalRecord.domain.services;
import com.backend.hormonalcare.medicalRecord.domain.model.aggregates.Patient;
import com.backend.hormonalcare.medicalRecord.domain.model.queries.*;

import java.util.List;
import java.util.Optional;

public interface PatientQueryService {
    Optional<Patient> handle(GetPatientByIdQuery query);
    Optional<Patient> handle(GetPatientByProfileIdQuery query);
    Optional<Patient> handle(GetPatientByPatientRecordIdQuery query);
    Optional<Long> handle(GetProfileIdByPatientIdQuery query);
    List<Patient> handle(GetAllPatientsByDoctorIdQuery query);
    List<Patient> handle(GetAllPatientsQuery query);

    List<Patient> handle(GetPatientsByNameQuery query);
}

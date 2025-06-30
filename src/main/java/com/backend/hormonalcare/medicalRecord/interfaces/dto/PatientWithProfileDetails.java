package com.backend.hormonalcare.medicalRecord.interfaces.dto;

import com.backend.hormonalcare.medicalRecord.domain.model.aggregates.Patient;
import com.backend.hormonalcare.profile.interfaces.acl.ProfileDetails;

public class PatientWithProfileDetails {
    private final Patient patient;
    private final ProfileDetails profileDetails;

    public PatientWithProfileDetails(Patient patient, ProfileDetails profileDetails) {
        this.patient = patient;
        this.profileDetails = profileDetails;
    }

    public Patient getPatient() {
        return patient;
    }

    public ProfileDetails getProfileDetails() {
        return profileDetails;
    }
}
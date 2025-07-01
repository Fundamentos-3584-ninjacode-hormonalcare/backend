package com.backend.hormonalcare.medicalRecord.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record FamilyHistory(String familyHistory) {
    public FamilyHistory {
    }
}

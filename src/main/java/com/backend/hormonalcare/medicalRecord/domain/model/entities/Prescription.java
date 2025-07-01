package com.backend.hormonalcare.medicalRecord.domain.model.entities;

import com.backend.hormonalcare.medicalRecord.domain.model.aggregates.MedicalRecord;
import com.backend.hormonalcare.medicalRecord.domain.model.commands.CreatePrescriptionCommand;
import com.backend.hormonalcare.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.Date;

@Getter
@Entity
public class Prescription extends AuditableModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @ManyToOne
    @JoinColumn(name = "medicalRecord_id",referencedColumnName = "id" )
    private MedicalRecord medicalRecord;

    private Date prescriptionDate;
    private String notes;

    public Prescription() {
    }

    public Prescription(MedicalRecord medicalRecord, Date prescriptionDate, String notes) {
        this.medicalRecord = medicalRecord;
        this.prescriptionDate = prescriptionDate;
        this.notes = notes;
    }

    public Prescription(CreatePrescriptionCommand command, MedicalRecord medicalRecord) {
        this.medicalRecord = medicalRecord;
        this.prescriptionDate = command.prescriptionDate();
        this.notes = command.notes();
    }

    public Prescription updateInformation(MedicalRecord medicalRecord, Date  prescriptionDate, String notes) {
        this.medicalRecord = medicalRecord;
        this.prescriptionDate = prescriptionDate;
        this.notes = notes;
        return this;
    }
}
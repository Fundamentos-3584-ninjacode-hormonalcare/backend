package com.backend.hormonalcare.medicalRecord.domain.model.aggregates;

import com.backend.hormonalcare.medicalRecord.domain.model.commands.CreateMedicalAppointmentCommand;
import com.backend.hormonalcare.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Entity
public class MedicalAppointment extends AuditableAbstractAggregateRoot<MedicalAppointment> {

    private LocalDate eventDate;

    private String startTime;

    private String endTime;

    private String title;

    private String description;

    private String color;

    @Getter
    @ManyToOne
    @JoinColumn(name = "patient_id",referencedColumnName = "id" )
    private Patient patient;


    @Getter
    @ManyToOne
    @JoinColumn(name = "doctor_id",referencedColumnName = "id" )
    private Doctor doctor;



    public MedicalAppointment() {
    }

    public MedicalAppointment(LocalDate eventDate, String startTime, String endTime, String title, String description, Patient patient  ,  Doctor doctor, String color) {
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.title = title;
        this.description = description;
        this.patient = patient;
        this.doctor = doctor;
        this.color = color;
    }

    public MedicalAppointment(CreateMedicalAppointmentCommand command, Patient patient  ,  Doctor doctor) {
        this.eventDate = command.eventDate();
        this.startTime = command.startTime();
        this.endTime = command.endTime();
        this.title = command.title();
        this.description = command.description();
        this.patient = patient;
        this.doctor = doctor;
        this.color = command.color();
    }

    public MedicalAppointment updateInformation(LocalDate eventDate, String startTime, String endTime, String title, String description, Patient patient  ,  Doctor doctor, String color) {
        this.eventDate =  eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.title = title;
        this.description = description;
        this.patient = patient;
        this.doctor = doctor;
        this.color = color;

        return this;
    }
}

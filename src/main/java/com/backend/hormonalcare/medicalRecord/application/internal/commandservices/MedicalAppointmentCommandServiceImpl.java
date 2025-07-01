package com.backend.hormonalcare.medicalRecord.application.internal.commandservices;

import com.backend.hormonalcare.medicalRecord.domain.model.aggregates.MedicalAppointment;
import com.backend.hormonalcare.medicalRecord.domain.model.commands.CreateMedicalAppointmentCommand;
import com.backend.hormonalcare.medicalRecord.domain.model.commands.DeleteMedicalAppointmentCommand;
import com.backend.hormonalcare.medicalRecord.domain.model.commands.UpdateMedicalAppointmentCommand;
import com.backend.hormonalcare.medicalRecord.domain.services.MedicalAppointmentCommandService;
import com.backend.hormonalcare.medicalRecord.infrastructure.persistence.jpa.repositories.DoctorRepository;
import com.backend.hormonalcare.medicalRecord.infrastructure.persistence.jpa.repositories.MedicalAppointmentRepository;
import com.backend.hormonalcare.medicalRecord.infrastructure.persistence.jpa.repositories.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MedicalAppointmentCommandServiceImpl implements MedicalAppointmentCommandService {

    private final MedicalAppointmentRepository medicalAppointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public MedicalAppointmentCommandServiceImpl(MedicalAppointmentRepository medicalAppointmentRepository, PatientRepository patientRepository, DoctorRepository doctorRepository) {
        this.medicalAppointmentRepository = medicalAppointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @Override
    public Optional<MedicalAppointment> handle(CreateMedicalAppointmentCommand command) {
        var patient = patientRepository.findById(command.patientId()).orElseThrow(() -> new RuntimeException("Patient not found"));
        var doctor = doctorRepository.findById(command.doctorId()).orElseThrow(() -> new RuntimeException("Doctor not found"));

        boolean exists = medicalAppointmentRepository.existsByDoctorIdAndEventDateAndStartTime(
                command.doctorId(), command.eventDate(), command.startTime());
        if (exists) {
            throw new IllegalArgumentException("A medical appointment already exists with the same event date and start time for this doctor.");
        }

        var medicalAppointment = new MedicalAppointment(command, patient, doctor);
        medicalAppointmentRepository.save(medicalAppointment);
        return Optional.of(medicalAppointment);
    }

    @Override
    public Optional<MedicalAppointment> handle(UpdateMedicalAppointmentCommand command) {
        var id = command.id();
        if(!medicalAppointmentRepository.existsById(id))
            throw new IllegalArgumentException("MedicalAppointment with id "+ id +" does not exist");
        var result = medicalAppointmentRepository.findById(id);
        var medicalAppointmentToUpdate = result.get();
        try{
            var patient = patientRepository.findById(command.patientId()).orElseThrow(() -> new RuntimeException("Patient not found"));
            var doctor = doctorRepository.findById(command.doctorId()).orElseThrow(() -> new RuntimeException("Doctor not found"));
            var updatedMedicalAppointment = medicalAppointmentRepository.save(medicalAppointmentToUpdate.updateInformation
                    (
                            command.eventDate(),
                            command.startTime(),
                            command.endTime(),
                            command.title(),
                            command.description(),
                            patient,
                            doctor,
                            command.color()
                    ));
            return Optional.of(updatedMedicalAppointment);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error updating medicalAppointment with id "+ id);
        }
    }


    @Override
    public void handle(DeleteMedicalAppointmentCommand command) {
        medicalAppointmentRepository.deleteById(command.id());
    }
}
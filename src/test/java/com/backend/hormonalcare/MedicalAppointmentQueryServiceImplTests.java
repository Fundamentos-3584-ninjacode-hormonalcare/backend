package com.backend.hormonalcare;

import com.backend.hormonalcare.medicalRecord.application.internal.queryservices.MedicalAppointmentQueryServiceImpl;
import com.backend.hormonalcare.medicalRecord.domain.model.aggregates.MedicalAppointment;
import com.backend.hormonalcare.medicalRecord.domain.model.queries.*;
import com.backend.hormonalcare.medicalRecord.infrastructure.persistence.jpa.repositories.MedicalAppointmentRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MedicalAppointmentQueryServiceImplTests {

    @Test
    void testHandleGetAllAppointments_returnsList() {
        MedicalAppointmentRepository repository = mock(MedicalAppointmentRepository.class);
        MedicalAppointmentQueryServiceImpl service = new MedicalAppointmentQueryServiceImpl(repository);

        when(repository.findAll()).thenReturn(List.of(new MedicalAppointment(), new MedicalAppointment()));

        List<MedicalAppointment> result = service.handle(new GetAllMedicalAppointmentQuery());

        assertEquals(2, result.size());
    }

    @Test
    void testHandleGetAppointmentById_found() {
        MedicalAppointmentRepository repository = mock(MedicalAppointmentRepository.class);
        MedicalAppointmentQueryServiceImpl service = new MedicalAppointmentQueryServiceImpl(repository);

        MedicalAppointment appointment = new MedicalAppointment();
        when(repository.findById(1L)).thenReturn(Optional.of(appointment));

        Optional<MedicalAppointment> result = service.handle(new GetMedicalAppointmentByIdQuery(1L));

        assertTrue(result.isPresent());
    }

    @Test
    void testHandleGetAppointmentById_notFound() {
        MedicalAppointmentRepository repository = mock(MedicalAppointmentRepository.class);
        MedicalAppointmentQueryServiceImpl service = new MedicalAppointmentQueryServiceImpl(repository);

        when(repository.findById(999L)).thenReturn(Optional.empty());

        Optional<MedicalAppointment> result = service.handle(new GetMedicalAppointmentByIdQuery(999L));

        assertFalse(result.isPresent());
    }
}

package com.backend.hormonalcare;

import com.backend.hormonalcare.medicalRecord.application.internal.commandservices.DoctorCommandServiceImpl;
import com.backend.hormonalcare.medicalRecord.application.internal.outboundservices.acl.ExternalProfileService;
import com.backend.hormonalcare.medicalRecord.domain.model.aggregates.Doctor;
import com.backend.hormonalcare.medicalRecord.domain.model.commands.CreateDoctorCommand;
import com.backend.hormonalcare.medicalRecord.domain.model.valueobjects.ProfileId;
import com.backend.hormonalcare.medicalRecord.infrastructure.persistence.jpa.repositories.DoctorRepository;
import com.backend.hormonalcare.medicalRecord.infrastructure.persistence.jpa.repositories.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorCommandServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private ExternalProfileService externalProfileService;
    @Mock private ApplicationEventPublisher publisher;

    private DoctorCommandServiceImpl doctorService;

    @BeforeEach
    void setUp() {
        doctorService = new DoctorCommandServiceImpl(doctorRepository, patientRepository, externalProfileService, publisher);
    }

    @Test
    void testHandleCreateDoctorSuccess() {
        CreateDoctorCommand command = new CreateDoctorCommand("John", "Doe", "M", "999999999", "profile.jpg", new Date(),1L,123456L,"Cardiology");

        when(externalProfileService.fetchProfileIdByPhoneNumber(any(String.class)))
                .thenReturn(Optional.empty());

        when(externalProfileService.createProfile(
                any(String.class),
                any(String.class),
                any(String.class),
                any(String.class),
                any(String.class),
                any(Date.class),
                any(Long.class)
        )).thenReturn(Optional.of(new ProfileId(42L)));


        Optional<Doctor> result = doctorService.handle(command);

        assertTrue(result.isPresent());
        assertEquals(42L, result.get().getProfileId());
    }
}

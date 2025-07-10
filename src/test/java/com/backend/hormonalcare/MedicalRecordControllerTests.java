package com.backend.hormonalcare;

import com.backend.hormonalcare.medicalRecord.domain.model.aggregates.MedicalRecord;
import com.backend.hormonalcare.medicalRecord.domain.model.commands.CreateMedicalRecordCommand;
import com.backend.hormonalcare.medicalRecord.domain.model.queries.GetMedicalRecordByIdQuery;
import com.backend.hormonalcare.medicalRecord.domain.services.MedicalRecordCommandService;
import com.backend.hormonalcare.medicalRecord.domain.services.MedicalRecordQueryService;
import com.backend.hormonalcare.medicalRecord.interfaces.rest.MedicalRecordController;
import com.backend.hormonalcare.medicalRecord.interfaces.rest.resources.CreateMedicalRecordResource;
import com.backend.hormonalcare.medicalRecord.interfaces.rest.resources.MedicalRecordResource;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MedicalRecordControllerTests {

    @Test
    void testCreateMedicalRecordReturnsCreated() {
        MedicalRecordCommandService commandService = mock(MedicalRecordCommandService.class);
        MedicalRecordQueryService queryService = mock(MedicalRecordQueryService.class);
        MedicalRecordController controller = new MedicalRecordController(commandService, queryService);

        CreateMedicalRecordResource resource = new CreateMedicalRecordResource(1L);
        MedicalRecord mockRecord = new MedicalRecord(1L);

        when(commandService.handle(any(CreateMedicalRecordCommand.class))).thenReturn(Optional.of(mockRecord));

        ResponseEntity<MedicalRecordResource> response = controller.createMedicalRecord(resource);

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().patientId());
    }

    @Test
    void testCreateMedicalRecordReturnsBadRequest() {
        MedicalRecordCommandService commandService = mock(MedicalRecordCommandService.class);
        MedicalRecordQueryService queryService = mock(MedicalRecordQueryService.class);
        MedicalRecordController controller = new MedicalRecordController(commandService, queryService);

        CreateMedicalRecordResource resource = new CreateMedicalRecordResource(99L);
        when(commandService.handle(any(CreateMedicalRecordCommand.class))).thenReturn(Optional.empty());

        ResponseEntity<MedicalRecordResource> response = controller.createMedicalRecord(resource);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
    }

    @Test
    void testGetMedicalRecordByIdReturnsRecord() {
        MedicalRecordCommandService commandService = mock(MedicalRecordCommandService.class);
        MedicalRecordQueryService queryService = mock(MedicalRecordQueryService.class);
        MedicalRecordController controller = new MedicalRecordController(commandService, queryService);

        MedicalRecord mockRecord = new MedicalRecord(3L);
        when(queryService.handle(any(GetMedicalRecordByIdQuery.class))).thenReturn(Optional.of(mockRecord));

        ResponseEntity<MedicalRecordResource> response = controller.getMedicalRecordById(1L);

        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        assertEquals(3L, response.getBody().patientId());
    }

    @Test
    void testGetMedicalRecordByIdReturnsNotFound() {
        MedicalRecordCommandService commandService = mock(MedicalRecordCommandService.class);
        MedicalRecordQueryService queryService = mock(MedicalRecordQueryService.class);
        MedicalRecordController controller = new MedicalRecordController(commandService, queryService);

        when(queryService.handle(any(GetMedicalRecordByIdQuery.class))).thenReturn(Optional.empty());

        ResponseEntity<MedicalRecordResource> response = controller.getMedicalRecordById(999L);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
    }
}

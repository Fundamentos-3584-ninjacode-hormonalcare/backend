package com.backend.hormonalcare.medicalRecord.interfaces.rest;

import com.backend.hormonalcare.medicalRecord.domain.model.commands.DeleteMedicalAppointmentCommand;
import com.backend.hormonalcare.medicalRecord.domain.model.queries.GetAllMedicalAppointmentQuery;
import com.backend.hormonalcare.medicalRecord.domain.model.queries.GetMedicalAppointmentByDoctorIdQuery;
import com.backend.hormonalcare.medicalRecord.domain.model.queries.GetMedicalAppointmentByIdQuery;
import com.backend.hormonalcare.medicalRecord.domain.services.MedicalAppointmentCommandService;
import com.backend.hormonalcare.medicalRecord.domain.services.MedicalAppointmentQueryService;
import com.backend.hormonalcare.medicalRecord.interfaces.rest.resources.CreateMedicalAppointmentResource;
import com.backend.hormonalcare.medicalRecord.interfaces.rest.resources.MedicalAppointmentResource;
import com.backend.hormonalcare.medicalRecord.interfaces.rest.resources.UpdateMedicalAppointmentResource;
import com.backend.hormonalcare.medicalRecord.interfaces.rest.transform.CreateMedicalAppointmentCommandFromResourceAssembler;
import com.backend.hormonalcare.medicalRecord.interfaces.rest.transform.MedicalAppointmentResourceFromEntityAssembler;
import com.backend.hormonalcare.medicalRecord.interfaces.rest.transform.UpdateMedicalAppointmentCommandFromResourceAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/medicalAppointment", produces = MediaType.APPLICATION_JSON_VALUE)
public class MedicalAppointmentController {
    private final MedicalAppointmentCommandService medicalAppointmentCommandService;
    private final MedicalAppointmentQueryService medicalAppointmentQueryService;

    public MedicalAppointmentController(MedicalAppointmentCommandService medicalAppointmentCommandService, MedicalAppointmentQueryService medicalAppointmentQueryService) {
        this.medicalAppointmentCommandService = medicalAppointmentCommandService;
        this.medicalAppointmentQueryService = medicalAppointmentQueryService;
    }

    @PostMapping
public ResponseEntity<MedicalAppointmentResource> createMedicalAppointment(@RequestBody CreateMedicalAppointmentResource resource){
    var createMedicalAppointmentCommand = CreateMedicalAppointmentCommandFromResourceAssembler.toCommandFromResource(resource);
    try {
        var medicalAppointment = medicalAppointmentCommandService.handle(createMedicalAppointmentCommand);
        if(medicalAppointment.isEmpty()) return ResponseEntity.badRequest().build();
        var medicalAppointmentResource = MedicalAppointmentResourceFromEntityAssembler.toResourceFromEntity(medicalAppointment.get());
        return new ResponseEntity<>(medicalAppointmentResource, HttpStatus.CREATED);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
    }
}
    @GetMapping
    public ResponseEntity<List<MedicalAppointmentResource>> getAllMedicalAppointments(){
        var medicalAppointments = medicalAppointmentQueryService.handle(new GetAllMedicalAppointmentQuery());
        var medicalAppointmentResources = medicalAppointments.stream()
                .map(MedicalAppointmentResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(medicalAppointmentResources);
    }
    @GetMapping("/{medicalAppointmentId}")
    public ResponseEntity<MedicalAppointmentResource> getMedicalAppointmentById(@PathVariable Long medicalAppointmentId){
        var getMedicalAppointmentByIdQuery = new GetMedicalAppointmentByIdQuery(medicalAppointmentId);
        var medicalAppointment = medicalAppointmentQueryService.handle(getMedicalAppointmentByIdQuery);
        if(medicalAppointment.isEmpty()) return ResponseEntity.notFound().build();
        var medicalAppointmentResource = MedicalAppointmentResourceFromEntityAssembler.toResourceFromEntity(medicalAppointment.get());
        return ResponseEntity.ok(medicalAppointmentResource);
    }

    @GetMapping("/medicalAppointments/doctor/{doctorId}")
    public ResponseEntity<List<MedicalAppointmentResource>> getMedicalAppointmentsByDoctorId(@PathVariable Long doctorId) {
        var query = new GetMedicalAppointmentByDoctorIdQuery(doctorId);
        var medicalAppointments = medicalAppointmentQueryService.handle(query);
        var medicalAppointmentResources = medicalAppointments.stream()
                .map(MedicalAppointmentResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(medicalAppointmentResources);
    }

    @GetMapping("/medicalAppointments/patient/{patientId}")
    public ResponseEntity<List<MedicalAppointmentResource>> getMedicalAppointmentsByPatientId(@PathVariable Long patientId) {
        var query = new GetMedicalAppointmentByDoctorIdQuery(patientId);
        var medicalAppointments = medicalAppointmentQueryService.handle(query);
        var medicalAppointmentResources = medicalAppointments.stream()
                .map(MedicalAppointmentResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(medicalAppointmentResources);
    }

    @PutMapping("/{medicalAppointmentId}")
    public ResponseEntity<MedicalAppointmentResource> updateMedicalAppointment(@PathVariable Long medicalAppointmentId, @RequestBody UpdateMedicalAppointmentResource resource){
        var updateMedicalAppointmentCommand = UpdateMedicalAppointmentCommandFromResourceAssembler.toCommandFromResource(medicalAppointmentId, resource);
        var updatedMedicalAppointment = medicalAppointmentCommandService.handle(updateMedicalAppointmentCommand);
        if(updatedMedicalAppointment.isEmpty()) return ResponseEntity.notFound().build();
        var updatedMedicalAppointmentResource = MedicalAppointmentResourceFromEntityAssembler.toResourceFromEntity(updatedMedicalAppointment.get());
        return ResponseEntity.ok(updatedMedicalAppointmentResource);
    }

    @DeleteMapping("/{medicalAppointmentId}")
    public ResponseEntity<Void> deleteMedicalAppointment(@PathVariable Long medicalAppointmentId){
        var deleteMedicalAppointmentCommand = new DeleteMedicalAppointmentCommand(medicalAppointmentId);
        medicalAppointmentCommandService.handle(deleteMedicalAppointmentCommand);
        return ResponseEntity.noContent().build();
    }
}
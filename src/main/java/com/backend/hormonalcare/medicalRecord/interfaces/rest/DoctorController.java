package com.backend.hormonalcare.medicalRecord.interfaces.rest;

import com.backend.hormonalcare.medicalRecord.domain.model.commands.CreateDoctorCommand;
import com.backend.hormonalcare.medicalRecord.domain.model.queries.GetAllDoctorsQuery;
import com.backend.hormonalcare.medicalRecord.domain.model.queries.GetDoctorByDoctorRecordIdQuery;
import com.backend.hormonalcare.medicalRecord.domain.model.queries.GetDoctorByIdQuery;
import com.backend.hormonalcare.medicalRecord.domain.model.queries.GetDoctorByProfileIdQuery;
import com.backend.hormonalcare.medicalRecord.domain.model.queries.GetProfileIdByDoctorIdQuery;
import com.backend.hormonalcare.medicalRecord.domain.model.valueobjects.DoctorRecordId;
import com.backend.hormonalcare.medicalRecord.domain.model.valueobjects.ProfileId;
import com.backend.hormonalcare.medicalRecord.domain.services.DoctorCommandService;
import com.backend.hormonalcare.medicalRecord.domain.services.DoctorQueryService;
import com.backend.hormonalcare.medicalRecord.interfaces.rest.resources.DoctorResource;
import com.backend.hormonalcare.medicalRecord.interfaces.rest.resources.DoctorWithProfileResource;
import com.backend.hormonalcare.medicalRecord.interfaces.rest.resources.UpdateDoctorResource;
import com.backend.hormonalcare.medicalRecord.interfaces.rest.transform.DoctorResourceFromEntityAssembler;
import com.backend.hormonalcare.medicalRecord.interfaces.rest.transform.DoctorWithProfileResourceFromEntityAssembler;
import com.backend.hormonalcare.medicalRecord.interfaces.rest.transform.UpdateDoctorCommandFromResourceAssembler;
import com.backend.hormonalcare.medicalRecord.application.internal.outboundservices.acl.ExternalProfileService;

import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.backend.hormonalcare.medicalRecord.application.internal.outboundservices.acl.SupabaseStorageServiceTypeUser;


@RestController
@RequestMapping(value = "/api/v1/doctor", produces = MediaType.APPLICATION_JSON_VALUE)
public class DoctorController {
    private final DoctorCommandService doctorCommandService;
    private final DoctorQueryService doctorQueryService;
    private final ExternalProfileService externalProfileService;
    private final SupabaseStorageServiceTypeUser supabaseStorageService;

    public DoctorController(DoctorCommandService doctorCommandService, DoctorQueryService doctorQueryService, ExternalProfileService externalProfileService, SupabaseStorageServiceTypeUser supabaseStorageService) {
        this.doctorCommandService = doctorCommandService;
        this.doctorQueryService = doctorQueryService;
        this.externalProfileService = externalProfileService;
        this.supabaseStorageService = supabaseStorageService;
    }

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DoctorWithProfileResource> createDoctor(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("gender") String gender,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("birthday") String birthday,
            @RequestParam("userId") Long userId,
            @RequestParam("professionalIdentificationNumber") Long professionalIdentificationNumber,
            @RequestParam("subSpecialty") String subSpecialty,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            String image = null;
            if (file != null && !file.isEmpty()) {
                image = supabaseStorageService.uploadFile(file.getBytes(), file.getOriginalFilename());
            }

            // Convertir el string de fecha a un objeto Date
            Date birthdayDate;
            try {
                birthdayDate = new SimpleDateFormat("yyyy-MM-dd").parse(birthday);
            } catch (ParseException e) {
                return ResponseEntity.badRequest().build();
            }

            // Crear el comando con la URL de la imagen
            var createDoctorCommand = new CreateDoctorCommand(
                    firstName,
                    lastName,
                    gender,
                    phoneNumber,
                    image,  // URL de la imagen subida
                    birthdayDate,
                    userId,
                    professionalIdentificationNumber,
                    subSpecialty
            );

            var doctorOptional = doctorCommandService.handle(createDoctorCommand);
            if (doctorOptional.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            var doctor = doctorOptional.get();
            var doctorWithProfileResource = new DoctorWithProfileResource(
                    doctor.getId(),
                    firstName + " " + lastName,
                    image,
                    gender,
                    phoneNumber,
                    birthday,
                    professionalIdentificationNumber,
                    subSpecialty,
                    doctor.getProfileId(),
                    doctor.getDoctorRecordId()
            );

            return new ResponseEntity<>(doctorWithProfileResource, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace(); // O usa un logger
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{doctorId}/profile-id")
    public ResponseEntity<Long> getProfileIdByDoctorId(@PathVariable Long doctorId) {
        var getProfileIdByDoctorIdQuery = new GetProfileIdByDoctorIdQuery(doctorId);
        var profileId = doctorQueryService.handle(getProfileIdByDoctorIdQuery);
        if (profileId.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(profileId.get());
    }

    @GetMapping("/profile/{profileId}")
    public ResponseEntity<DoctorResource> getDoctorByProfileId(@PathVariable Long profileId) {
        var getDoctorByProfileIdQuery = new GetDoctorByProfileIdQuery(new ProfileId(profileId));
        var doctor = doctorQueryService.handle(getDoctorByProfileIdQuery);
        if (doctor.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var doctorResource = DoctorResourceFromEntityAssembler.toResourceFromEntity(doctor.get());
        return ResponseEntity.ok(doctorResource);
    }

    @GetMapping("/record/{doctorRecordId}")
    public ResponseEntity<DoctorResource> getDoctorByDoctorRecordId(@PathVariable String doctorRecordId) {
        var DoctorRecordId = new DoctorRecordId(doctorRecordId);
        var getDoctorByDoctorRecordIdQuery = new GetDoctorByDoctorRecordIdQuery(DoctorRecordId);
        var doctor = doctorQueryService.handle(getDoctorByDoctorRecordIdQuery);
        if (doctor.isEmpty()) {return ResponseEntity.notFound().build();}
        var doctorResource = DoctorResourceFromEntityAssembler.toResourceFromEntity(doctor.get());
        return ResponseEntity.ok(doctorResource);
    }

    @GetMapping("/{doctorId}")
    public ResponseEntity<DoctorWithProfileResource> getDoctorById(@PathVariable Long doctorId) {
        var getDoctorByIdQuery = new GetDoctorByIdQuery(doctorId);
        var doctorOptional = doctorQueryService.handle(getDoctorByIdQuery);

        if (doctorOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var doctor = doctorOptional.get();
        var profileDetailsOptional = externalProfileService.fetchProfileDetails(doctor.getProfileId());
        var profileDetails = profileDetailsOptional.orElse(null);

        var doctorWithProfileResource = DoctorWithProfileResourceFromEntityAssembler.toResourceFromEntity(doctor, profileDetails);
        return ResponseEntity.ok(doctorWithProfileResource);
    }

    @PutMapping("/{doctorId}")
    public ResponseEntity<DoctorResource> updateDoctor(@PathVariable Long doctorId, @RequestBody UpdateDoctorResource updateDoctorResource){
        var updateDoctorCommand = UpdateDoctorCommandFromResourceAssembler.toCommandFromResource(doctorId, updateDoctorResource);
        var updatedDoctor = doctorCommandService.handle(updateDoctorCommand);
        if(updatedDoctor.isEmpty()) return ResponseEntity.notFound().build();
        var doctorResource = DoctorResourceFromEntityAssembler.toResourceFromEntity(updatedDoctor.get());
        return ResponseEntity.ok(doctorResource);
    }

    @GetMapping
    public ResponseEntity<List<DoctorWithProfileResource>> getAllDoctors() {
        var doctors = doctorQueryService.handle(new GetAllDoctorsQuery());
        var doctorWithProfileResources = doctors.stream().map(doctor -> {
            var profileDetailsOptional = externalProfileService.fetchProfileDetails(doctor.getProfileId());
            var profileDetails = profileDetailsOptional.orElse(null);
            return DoctorWithProfileResourceFromEntityAssembler.toResourceFromEntity(doctor, profileDetails);
        }).toList();
        return ResponseEntity.ok(doctorWithProfileResources);
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<DoctorWithProfileResource> getDoctorByUserId(@PathVariable Long userId) {
        var doctorOptional = doctorQueryService.findDoctorByUserId(userId);
        if (doctorOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var doctor = doctorOptional.get();
        var profileDetailsOptional = externalProfileService.fetchProfileDetails(doctor.getProfileId());
        var profileDetails = profileDetailsOptional.orElse(null);

        var doctorWithProfileResource = DoctorWithProfileResourceFromEntityAssembler.toResourceFromEntity(doctor, profileDetails);
        return ResponseEntity.ok(doctorWithProfileResource);
    }
}
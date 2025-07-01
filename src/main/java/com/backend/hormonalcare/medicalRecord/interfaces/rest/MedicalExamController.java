package com.backend.hormonalcare.medicalRecord.interfaces.rest;
import com.backend.hormonalcare.medicalRecord.domain.model.commands.CreateMedicalExamCommand;
import com.backend.hormonalcare.medicalRecord.domain.model.commands.DeleteMedicalExamCommand;
import com.backend.hormonalcare.medicalRecord.domain.model.queries.GetMedicalExamByIdQuery;
import com.backend.hormonalcare.medicalRecord.domain.model.queries.GetMedicalExamByMedicalRecordIdQuery;
import com.backend.hormonalcare.medicalRecord.domain.model.valueobjects.TypeMedicalExam;
import com.backend.hormonalcare.medicalRecord.domain.services.MedicalExamCommandService;
import com.backend.hormonalcare.medicalRecord.domain.services.MedicalExamQueryService;
import com.backend.hormonalcare.medicalRecord.interfaces.rest.resources.MedicalExamResource;
import com.backend.hormonalcare.medicalRecord.interfaces.rest.resources.UpdateMedicalExamResource;
import com.backend.hormonalcare.medicalRecord.interfaces.rest.transform.MedicalExamResourceFromEntityAssembler;
import com.backend.hormonalcare.medicalRecord.interfaces.rest.transform.UpdateMedicalExamCommandFromResourceAssembler;
import java.io.IOException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.backend.hormonalcare.medicalRecord.application.internal.outboundservices.acl.SupabaseStorageServiceMedicalExam;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value="/api/v1/medical-record/medical-exam", produces = MediaType.APPLICATION_JSON_VALUE)
public class MedicalExamController {


    private final MedicalExamCommandService medicalExamCommandService;
    private final MedicalExamQueryService medicalExamQueryService;
    private final SupabaseStorageServiceMedicalExam supabaseStorageService;

    public MedicalExamController(SupabaseStorageServiceMedicalExam supabaseStorageService,MedicalExamCommandService medicalExamCommandService, MedicalExamQueryService medicalExamQueryService) {
        this.supabaseStorageService = supabaseStorageService;
        this.medicalExamCommandService = medicalExamCommandService;
        this.medicalExamQueryService = medicalExamQueryService;
    }

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MedicalExamResource> createMedicalExam(
            @RequestParam("typeMedicalExam") TypeMedicalExam typeMedicalExam,
            @RequestParam("uploadDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate uploadDate,
            @RequestParam("medicalRecordId") Long medicalRecordId,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }


            final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body(null);
            }


            String url = supabaseStorageService.uploadFile(file.getBytes(), file.getOriginalFilename());


            if (url == null || url.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }


            var command = new CreateMedicalExamCommand(
                    url,
                    typeMedicalExam,
                    uploadDate,
                    medicalRecordId
            );


            var medicalExam = medicalExamCommandService.handle(command);
            if (medicalExam.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            var resource = MedicalExamResourceFromEntityAssembler.toResourceFromEntity(medicalExam.get());
            return new ResponseEntity<>(resource, HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }


    }



    @GetMapping("/medicalRecordId/{medicalRecordId}")
    public ResponseEntity<List<MedicalExamResource>> getMedicalExamsByMedicalRecordId(@PathVariable Long medicalRecordId) {
        var getMedicalExamsByMedicalRecordIdQuery = new GetMedicalExamByMedicalRecordIdQuery(medicalRecordId);
        var medicalExams = medicalExamQueryService.handle(getMedicalExamsByMedicalRecordIdQuery);
        if (medicalExams.isEmpty()) return ResponseEntity.notFound().build();
        var medicalExamResources = medicalExams.stream()
                .map(MedicalExamResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(medicalExamResources);
    }

    @DeleteMapping("/{medicalExamId}")
    public ResponseEntity<Void> deleteMedicalExam(@PathVariable Long medicalExamId) {
        var deleteMedicalExamCommand = new DeleteMedicalExamCommand(medicalExamId);
        var medicalExam = medicalExamQueryService.handle(new GetMedicalExamByIdQuery(medicalExamId));

        if (medicalExam.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            String filePath = medicalExam.get().getUrl().replace(supabaseStorageService.getProperties().getUrl() + "/storage/v1/object/public/" + supabaseStorageService.getProperties().getBucket() + "/", "");
            supabaseStorageService.deleteFile(filePath);
        } catch (IOException e) {
            e.printStackTrace(); // Log the error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        medicalExamCommandService.handle(deleteMedicalExamCommand);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{medicalExamId}")
    public ResponseEntity<MedicalExamResource> getMedicalExamById(@PathVariable Long medicalExamId) {
        var getMedicalExamByIdQuery = new GetMedicalExamByIdQuery(medicalExamId);
        var medicalExam = medicalExamQueryService.handle(getMedicalExamByIdQuery);
        if (medicalExam.isEmpty()) return ResponseEntity.notFound().build();
        var medicalExamResource = MedicalExamResourceFromEntityAssembler.toResourceFromEntity(medicalExam.get());
        return ResponseEntity.ok(medicalExamResource);
    }

    // @PutMapping(value = "/{medicalExamId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // public ResponseEntity<MedicalExamResource> updateMedicalExam(@PathVariable Long medicalExamId, @RequestBody UpdateMedicalExamResource updateMedicalExamResource) {
    //     var updateMedicalExamCommand = UpdateMedicalExamCommandFromResourceAssembler.toCommandFromResource(medicalExamId, updateMedicalExamResource);
    //     var updatedMedicalExam = medicalExamCommandService.handle(updateMedicalExamCommand);
    //     if (updatedMedicalExam.isEmpty()) return ResponseEntity.badRequest().build();
    //     var medicalExamResource = MedicalExamResourceFromEntityAssembler.toResourceFromEntity(updatedMedicalExam.get());
    //     return ResponseEntity.ok(medicalExamResource);
    // }

    @PutMapping(value = "/{medicalExamId}/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MedicalExamResource> updateMedicalExamFile(@PathVariable Long medicalExamId, @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            String newUrl = supabaseStorageService.uploadFile(file.getBytes(), file.getOriginalFilename());

            if (newUrl == null || newUrl.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            var currentMedicalExam = medicalExamQueryService.handle(new GetMedicalExamByIdQuery(medicalExamId));
            if (currentMedicalExam.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            String oldFileUrl = currentMedicalExam.get().getUrl();
            if (oldFileUrl != null && !oldFileUrl.isEmpty()) {
                String oldFilePath = oldFileUrl.replace(supabaseStorageService.getProperties().getUrl() + "/storage/v1/object/public/" + supabaseStorageService.getProperties().getBucket() + "/", "");
                supabaseStorageService.deleteFile(oldFilePath);
            }

            var updateResource = new UpdateMedicalExamResource(
                newUrl,
                currentMedicalExam.get().getTypeMedicalExam(),
                currentMedicalExam.get().getUploadDate(),
                currentMedicalExam.get().getMedicalRecord().getId()
            );

            var updateCommand = UpdateMedicalExamCommandFromResourceAssembler.toCommandFromResource(medicalExamId, updateResource);

            var updatedMedicalExam = medicalExamCommandService.handle(updateCommand);
            if (updatedMedicalExam.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            var resource = MedicalExamResourceFromEntityAssembler.toResourceFromEntity(updatedMedicalExam.get());
            return ResponseEntity.ok(resource);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

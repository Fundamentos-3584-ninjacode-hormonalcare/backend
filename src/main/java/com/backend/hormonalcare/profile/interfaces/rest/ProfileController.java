package com.backend.hormonalcare.profile.interfaces.rest;

import com.backend.hormonalcare.profile.domain.model.queries.GetProfileByIdQuery;
import com.backend.hormonalcare.profile.domain.model.queries.GetProfileByNameQuery;
import com.backend.hormonalcare.profile.domain.model.queries.GetProfileByUserIdQuery;
import com.backend.hormonalcare.profile.domain.services.ProfileCommandService;
import com.backend.hormonalcare.profile.domain.services.ProfileQueryService;
import com.backend.hormonalcare.profile.interfaces.rest.resources.*;
import com.backend.hormonalcare.profile.interfaces.rest.transform.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.backend.hormonalcare.profile.application.internal.outboundservices.acl.SupabaseStorageServiceProfile;
import com.backend.hormonalcare.profile.domain.model.aggregates.Profile;
import com.backend.hormonalcare.profile.domain.model.commands.CreateProfileCommand;
import com.backend.hormonalcare.profile.domain.model.commands.DeleteProfileImageCommand;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


@RestController
@RequestMapping(value = "/api/v1/profile", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProfileController {
    private final ProfileCommandService profileCommandService;
    private final ProfileQueryService profileQueryService;
    private final SupabaseStorageServiceProfile supabaseStorageService;

    public ProfileController(ProfileCommandService profileCommandService, ProfileQueryService profileQueryService, SupabaseStorageServiceProfile supabaseStorageService) {
        this.profileCommandService = profileCommandService;
        this.profileQueryService = profileQueryService;
        this.supabaseStorageService = supabaseStorageService;
    }

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileResource> createProfile(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("gender") String gender,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("birthday") String birthday,
            @RequestParam("userId") Long userId,
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
            var createProfileCommand = new CreateProfileCommand(
                    firstName,
                    lastName,
                    gender,
                    phoneNumber,
                    image,  // URL de la imagen subida
                    birthdayDate,
                    userId
            );

            var profile = profileCommandService.handle(createProfileCommand);
            if (profile.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            var profileResource = ProfileResourceFromEntityAssembler.toResourceFromEntity(profile.get());
            return new ResponseEntity<>(profileResource, HttpStatus.CREATED);
        }  catch (Exception e) {
            e.printStackTrace(); // O usa un logger
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }

    @PostMapping("/test")




    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Endpoint funcionando correctamente");
    }

    @GetMapping("/userId/exists/{userId}")
    public ResponseEntity<Boolean> doesProfileExistByUserId(@PathVariable Long userId) {
        var getProfileByUserIdQuery = new GetProfileByUserIdQuery(userId);
        var doesProfileExist = profileQueryService.doesProfileExist(getProfileByUserIdQuery);
        return ResponseEntity.ok(doesProfileExist);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProfileResource>> getProfilesByName(@RequestParam String name) {
        var query = new GetProfileByNameQuery(name);
        var profiles = profileQueryService.handle(query);
        var resources = profiles.stream()
                .map(ProfileResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/userId/{userId}")
    public ResponseEntity<ProfileResource> getProfileByUserId(@PathVariable Long userId) {
        var getProfileByUserIdQuery = new GetProfileByUserIdQuery(userId);
        var profile = profileQueryService.handle(getProfileByUserIdQuery);
        if (profile.isEmpty()) return ResponseEntity.notFound().build();
        var profileResource = ProfileResourceFromEntityAssembler.toResourceFromEntity(profile.get());
        return ResponseEntity.ok(profileResource);
    }

    @GetMapping("/{profileId}")
    public ResponseEntity<ProfileResource> getProfileById(@PathVariable Long profileId){
        var getProfileByIdQuery = new GetProfileByIdQuery(profileId);
        var profile = profileQueryService.handle(getProfileByIdQuery);
        if(profile.isEmpty()) return ResponseEntity.notFound().build();
        var profileResource = ProfileResourceFromEntityAssembler.toResourceFromEntity(profile.get());
        return ResponseEntity.ok(profileResource);
    }

    @PutMapping("/{profileId}/full-update")
    public ResponseEntity<ProfileResource> updateProfile(@PathVariable Long profileId, @RequestBody UpdateProfileResource updateProfileResource){
        var updateProfileCommand = UpdateProfileCommandFromResourceAssembler.toCommandFromResource(profileId, updateProfileResource);
        var updateProfile = profileCommandService.handle(updateProfileCommand);
        if(updateProfile.isEmpty()) return ResponseEntity.notFound().build();
        var profileResource = ProfileResourceFromEntityAssembler.toResourceFromEntity(updateProfile.get());
        return ResponseEntity.ok(profileResource);
    }

    @PutMapping(value = "/{profileId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileResource> updateProfileImage(@PathVariable Long profileId, @RequestParam("file") MultipartFile file) {
        try {
            // Verificar si el archivo está vacío
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Subir el archivo y obtener la URL
            String url = supabaseStorageService.uploadFile(file.getBytes(), file.getOriginalFilename());

            // Si la URL es nula o vacía, devolver un error
            if (url == null || url.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            // Obtener el perfil actual para eliminar la imagen antigua
            var currentProfile = profileQueryService.handle(new GetProfileByIdQuery(profileId));
            if (currentProfile.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            String oldImageUrl = currentProfile.get().getImage();
            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                String oldImagePath = oldImageUrl.replace(supabaseStorageService.getProperties().getUrl() + "/storage/v1/object/public/" + supabaseStorageService.getProperties().getBucket() + "/", "");
                supabaseStorageService.deleteFile(oldImagePath);
            }

            // Crear el comando para actualizar la imagen del perfil
            var updateProfileImageCommand = UpdateProfileImageCommandFromResourceAssembler.toCommandFromUrl(profileId, url);
            var updateProfileImage = profileCommandService.handle(updateProfileImageCommand);

            if (updateProfileImage.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            Profile updatedProfile = updateProfileImage.get();
            var profileResource = ProfileResourceFromEntityAssembler.toResourceFromEntity(updatedProfile);
            return ResponseEntity.ok(profileResource);

        } catch (IOException e) {
            e.printStackTrace(); // O usa un logger
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{profileId}/phoneNumber")
    public ResponseEntity<ProfileResource> updateProfilePhoneNumber(@PathVariable Long profileId, @RequestBody UpdateProfilePhoneNumberResource updateProfilePhoneNumberResource){
        var updateProfilePhoneNumberCommand = UpdateProfilePhoneNumberCommandFromResourceAssembler.toCommandFromResource(profileId, updateProfilePhoneNumberResource);
        var updateProfilePhoneNumber = profileCommandService.handle(updateProfilePhoneNumberCommand);
        if(updateProfilePhoneNumber.isEmpty()) return ResponseEntity.notFound().build();
        var profileResource = ProfileResourceFromEntityAssembler.toResourceFromEntity(updateProfilePhoneNumber.get());
        return ResponseEntity.ok(profileResource);

    }

    @DeleteMapping(value = "/{profileId}/image")
    public ResponseEntity<Void> deleteProfileImage(@PathVariable Long profileId) {
        try {
            var deleteProfileImageCommand = new DeleteProfileImageCommand(profileId);
            profileCommandService.handle(deleteProfileImageCommand);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace(); // O usa un logger
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}


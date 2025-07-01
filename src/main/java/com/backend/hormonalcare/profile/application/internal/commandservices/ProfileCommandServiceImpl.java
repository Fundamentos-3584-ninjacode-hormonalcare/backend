package com.backend.hormonalcare.profile.application.internal.commandservices;

import com.backend.hormonalcare.iam.domain.model.aggregates.User;
import com.backend.hormonalcare.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.backend.hormonalcare.profile.domain.model.aggregates.Profile;
import com.backend.hormonalcare.profile.domain.model.commands.CreateProfileCommand;
import com.backend.hormonalcare.profile.domain.model.commands.UpdateProfileCommand;
import com.backend.hormonalcare.profile.domain.model.commands.UpdateProfileImageCommand;
import com.backend.hormonalcare.profile.domain.model.commands.UpdateProfilePhoneNumberCommand;
import com.backend.hormonalcare.profile.domain.model.commands.DeleteProfileImageCommand;
import com.backend.hormonalcare.profile.domain.model.valueobjects.PhoneNumber;
import com.backend.hormonalcare.profile.domain.services.ProfileCommandService;
import com.backend.hormonalcare.profile.infrastructure.persistence.jpa.repositories.ProfileRepository;
import com.backend.hormonalcare.profile.application.internal.outboundservices.acl.SupabaseStorageServiceProfile;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.io.IOException;

@Service
public class ProfileCommandServiceImpl implements ProfileCommandService {
    private static final Logger logger = LoggerFactory.getLogger(ProfileCommandServiceImpl.class);
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final SupabaseStorageServiceProfile supabaseStorageService;

    public ProfileCommandServiceImpl(ProfileRepository profileRepository, UserRepository userRepository, SupabaseStorageServiceProfile supabaseStorageService) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.supabaseStorageService = supabaseStorageService;
    }

    @Override
    public Optional<Profile> handle(CreateProfileCommand command) {
        User user = userRepository.findById(command.userId()).orElseThrow(() -> new IllegalArgumentException("User with id " + command.userId() + " does not exist"));
        PhoneNumber phoneNumber = new PhoneNumber(command.phoneNumber());
        if (profileRepository.existsByPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("Profile with phone number " + command.phoneNumber() + " already exists");
        }
        // Check if the user already has a profile
        if (profileRepository.existsByUserId(command.userId())) {
            throw new IllegalArgumentException("User with id " + command.userId() + " already has a profile");
        }

        // Handle image upload if provided
        
        var profile = new Profile(command, user);
        profileRepository.save(profile);
        logger.info("Profile created successfully for user with id: " + command.userId());
        return Optional.of(profile);
    }

    @Override
    public Optional<Profile> handle(UpdateProfilePhoneNumberCommand command) {
        var id = command.id();
        if (!profileRepository.existsById(id))
            throw new IllegalArgumentException("Profile with id " + id + " does not exist");
        var result = profileRepository.findById(id);
        var profileToUpdate = result.get();
        try {
            PhoneNumber phoneNumber = new PhoneNumber(command.phoneNumber());
            var updateProfile = profileRepository.save(profileToUpdate.upsetPhoneNumber(phoneNumber));
            return Optional.of(updateProfile);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error updating profile with id " + id);
        }
    }

    @Override
    public Optional<Profile> handle(UpdateProfileImageCommand command) {
        var id = command.id();
        if (!profileRepository.existsById(id))
            throw new IllegalArgumentException("Profile with id "+ id +" does not exist");
        var result = profileRepository.findById(id);
        var profileToUpdate = result.get();
        try {
            var updateProfile = profileRepository.save(profileToUpdate.upsetImage(command.image()));
            return Optional.of(updateProfile);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error updating profile with id "+ id);
        }
    }

    @Override
    public Optional<Profile> handle(UpdateProfileCommand command) {
        var id = command.id();
        if (!profileRepository.existsById(id))
            throw new IllegalArgumentException("Profile with id "+ id +" does not exist");
        var result = profileRepository.findById(id);
        var profileToUpdate = result.get();
        try {
            var updateProfile = profileRepository.save(profileToUpdate.updateProfile(command.firstName(), command.lastName(), command.gender(), command.phoneNumber(), command.image(), command.birthday()));
            return Optional.of(updateProfile);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error updating profile with id "+ id);
        }
    }

    @Override
    public Optional<Void> handle(DeleteProfileImageCommand command) {
        var profile = profileRepository.findById(command.getProfileId())
                .orElseThrow(() -> new IllegalArgumentException("Profile with id " + command.getProfileId() + " does not exist"));

        if (profile.getImage() != null && !profile.getImage().isEmpty()) {
            String imagePath = profile.getImage().replace(supabaseStorageService.getProperties().getUrl() + "/storage/v1/object/public/" + supabaseStorageService.getProperties().getBucket() + "/", "");
            try {
                supabaseStorageService.deleteFile(imagePath);
                profile.upsetImage(null);
                profileRepository.save(profile);
            } catch (IOException e) {
                throw new RuntimeException("Error deleting profile image: " + e.getMessage(), e);
            }
        }

        return Optional.empty();
    }
}

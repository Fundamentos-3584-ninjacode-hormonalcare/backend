package com.backend.hormonalcare.profile.application.internal.queryservices;

import com.backend.hormonalcare.profile.domain.model.aggregates.Profile;
import com.backend.hormonalcare.profile.domain.model.queries.*;
import com.backend.hormonalcare.profile.domain.services.ProfileQueryService;
import com.backend.hormonalcare.profile.infrastructure.persistence.jpa.repositories.ProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class ProfileQueryServiceImpl implements ProfileQueryService {
    private final ProfileRepository profileRepository;

    public ProfileQueryServiceImpl(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public List<Profile> handle(GetAllProfilesQuery query) {
        return List.of();
    }

    @Override
    public Optional<Profile> handle(GetProfileByIdQuery query) {
        return profileRepository.findById(query.id());
    }

    @Override
    public List<Profile> findByName_FirstNameContainingIgnoreCase(String firstName) {
        return profileRepository.findByName_FirstNameContainingIgnoreCase(firstName);
    }
    @Override
    public Optional<Profile> handle(GetProfileByPhoneNumberQuery query) {
        return profileRepository.findByPhoneNumber(query.phoneNumber());
    }
    @Override
    public List<Profile> handle(GetProfileByNameQuery query) {
        return profileRepository.findByName_FirstNameContainingIgnoreCase(query.name());
    }

    @Override
    public List<Profile> handle(GetProfileByLastNameQuery query) {
        return profileRepository.findByName_LastNameContainingIgnoreCase(query.lastName());
    }

    @Override
    public boolean doesProfileExist(GetProfileByUserIdQuery query) {
        return profileRepository.existsByUserId(query.userId());
    }

    @Override
    public Optional<Profile> handle(GetProfileByUserIdQuery query) {
        return profileRepository.findByUserId(query.userId());
    }

    @Override
    public Optional<Profile> findByUserId(Long userId) {
        return profileRepository.findByUserId(userId);
    }
}
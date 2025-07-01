package com.backend.hormonalcare.profile.interfaces.acl;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.backend.hormonalcare.profile.domain.model.commands.CreateProfileCommand;
import com.backend.hormonalcare.profile.domain.model.commands.UpdateProfileCommand;
import com.backend.hormonalcare.profile.domain.model.queries.GetProfileByIdQuery;
import com.backend.hormonalcare.profile.domain.model.queries.GetProfileByPhoneNumberQuery;
import com.backend.hormonalcare.profile.domain.model.valueobjects.PhoneNumber;
import com.backend.hormonalcare.profile.domain.services.ProfileCommandService;
import com.backend.hormonalcare.profile.domain.services.ProfileQueryService;
import com.backend.hormonalcare.profile.domain.model.aggregates.Profile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfileContextFacade {
    private final ProfileCommandService profileCommandService;
    private final ProfileQueryService profileQueryService;

    public ProfileContextFacade(ProfileCommandService profileCommandService, ProfileQueryService profileQueryService) {
        this.profileCommandService = profileCommandService;
        this.profileQueryService = profileQueryService;

    }

    public Long createProfile(String firstName, String lastName, String gender, String phoneNumber, String image, Date birthday, Long userId){
        var createProfileCommand = new CreateProfileCommand(firstName, lastName, gender, phoneNumber, image, birthday, userId);
        var profile = profileCommandService.handle(createProfileCommand);
        if (profile.isEmpty()) return 0L;
        return profile.get().getId();
    }

    public boolean updateProfile(Long profileId, String firstName, String lastName, String gender, String phoneNumber, String image, Date birthday) {
        var updateProfileCommand = new UpdateProfileCommand(profileId, firstName, lastName, gender, phoneNumber, image, birthday);
        var updatedProfile = profileCommandService.handle(updateProfileCommand);
        return updatedProfile.isPresent();
    }

    public Long fetchProfileIdByPhoneNumber(String phoneNumber) {
        var getProfileByPhoneNumberQuery = new GetProfileByPhoneNumberQuery(new PhoneNumber(phoneNumber));
        var profile = profileQueryService.handle(getProfileByPhoneNumberQuery);
        if (profile.isEmpty()) return 0L;
        return profile.get().getId();
    }


        // Add these new methods for Communication bounded context
    
    public boolean profileExists(Long profileId) {
        var getProfileByIdQuery = new GetProfileByIdQuery(profileId);
        var profile = profileQueryService.handle(getProfileByIdQuery);
        return profile.isPresent();
    }
    
    public String getProfileFullName(Long profileId) {
        var getProfileByIdQuery = new GetProfileByIdQuery(profileId);
        var profile = profileQueryService.handle(getProfileByIdQuery);
        if (profile.isEmpty()) return "";
        
        return profile.get().getName().firstName() + " " + profile.get().getName().lastName();
    }

    public String getProfileImage(Long profileId) {
        var getProfileByIdQuery = new GetProfileByIdQuery(profileId);
        var profile = profileQueryService.handle(getProfileByIdQuery);
        if (profile.isEmpty()) return "";
        
        return profile.get().getImage();
    }

    public String getGender(Long profileId) {
        var getProfileByIdQuery = new GetProfileByIdQuery(profileId);
        var profile = profileQueryService.handle(getProfileByIdQuery);
        if (profile.isEmpty()) return "";
    
        // Accedemos al valor interno del objeto Gender
        return profile.get().getGender().gender();
    }
    
    public String getProfilePhoneNumber(Long profileId) {
        var getProfileByIdQuery = new GetProfileByIdQuery(profileId);
        var profile = profileQueryService.handle(getProfileByIdQuery);
        if (profile.isEmpty()) return "";
    
        // Accedemos al valor interno del objeto PhoneNumber
        return profile.get().getPhoneNumber().phoneNumber();
    }
    
    public String getProfileBirthday(Long profileId) {
        var getProfileByIdQuery = new GetProfileByIdQuery(profileId);
        var profile = profileQueryService.handle(getProfileByIdQuery);
        if (profile.isEmpty()) return "";
    
        // Convertimos el objeto Birthday a una cadena en formato ISO 8601
        return profile.get().getBirthday().birthday().toString();
    }

    public List<ProfileDetails> findProfileDetailsByFirstName(String firstName) {
        return findProfilesByFirstName(firstName).stream()
                .map(profile -> new ProfileDetails(
                        profile.getId(),
                        profile.getName().firstName() + " " + profile.getName().lastName(),
                        profile.getImage(),
                        profile.getGender().gender(),
                        profile.getPhoneNumber().phoneNumber(),
                        profile.getBirthday().birthday().toString()
                ))
                .collect(Collectors.toList());
    }

    public List<Profile> findProfilesByFirstName(String firstName) {
        return profileQueryService.findByName_FirstNameContainingIgnoreCase(firstName);
    }

    public Long fetchProfileIdByUserId(Long userId) {
        var profileOpt = profileQueryService.findByUserId(userId);
        if (profileOpt.isEmpty()) return 0L;
        return profileOpt.get().getId();
    }
}

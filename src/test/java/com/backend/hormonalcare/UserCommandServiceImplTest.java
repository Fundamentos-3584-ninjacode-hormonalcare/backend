package com.backend.hormonalcare;

import com.backend.hormonalcare.iam.application.internal.commandservices.UserCommandServiceImpl;
import com.backend.hormonalcare.iam.application.internal.outboundservices.hashing.HashingService;
import com.backend.hormonalcare.iam.application.internal.outboundservices.tokens.TokenService;
import com.backend.hormonalcare.iam.domain.model.aggregates.User;
import com.backend.hormonalcare.iam.domain.model.commands.SignUpCommand;
import com.backend.hormonalcare.iam.domain.model.entities.Role;
import com.backend.hormonalcare.iam.domain.model.valueobjects.Roles;
import com.backend.hormonalcare.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.backend.hormonalcare.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock private HashingService hashingService;
    @Mock private TokenService tokenService;
    @Mock private RoleRepository roleRepository;

    private UserCommandServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserCommandServiceImpl(userRepository, hashingService, tokenService, roleRepository);
    }

    @Test
    void testSignUpCreatesUserSuccessfully() {
        SignUpCommand command = new SignUpCommand("user", "pass", new ArrayList<>());
        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(roleRepository.findByName(Roles.ROLE_USER)).thenReturn(Optional.of(new Role(Roles.ROLE_USER)));
        when(hashingService.encode("pass")).thenReturn("hashed");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(new User("user", "hashed")));

        Optional<User> result = userService.handle(command);

        assertTrue(result.isPresent());
        assertEquals("user", result.get().getUsername());
    }
}

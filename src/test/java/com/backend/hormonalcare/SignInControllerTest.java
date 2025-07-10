package com.backend.hormonalcare;

import com.backend.hormonalcare.iam.domain.model.commands.SignInCommand;
import com.backend.hormonalcare.iam.domain.services.UserCommandService;
import com.backend.hormonalcare.iam.interfaces.rest.AuthenticationController;
import com.backend.hormonalcare.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.backend.hormonalcare.iam.interfaces.rest.resources.SignInResource;
import com.backend.hormonalcare.iam.domain.model.entities.Role;
import com.backend.hormonalcare.iam.domain.model.valueobjects.Roles;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SignInControllerTest {

    @Test
    public void testSignInReturnsAuthenticatedUserResource() {
        UserCommandService userCommandService = mock(UserCommandService.class);
        AuthenticationController controller = new AuthenticationController(userCommandService);

        // Ensure SignInResource instantiation matches its constructor
        SignInResource resource = new SignInResource("user@example.com", "password123");

        // Simulate a valid response
        var mockUser = new com.backend.hormonalcare.iam.domain.model.aggregates.User();
        mockUser.addRole(new Role(Roles.ROLE_USER)); // Use Roles enum or object to assign a role

        var mockResponse = Optional.of(ImmutablePair.of(mockUser, "mocked-jwt-token"));

        // Resolve ambiguity by specifying the argument type
        when(userCommandService.handle(any(SignInCommand.class))).thenReturn(mockResponse);

        ResponseEntity<AuthenticatedUserResource> response = controller.signIn(resource);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("mocked-jwt-token", response.getBody().token());
    }

    @Test
    public void testSignInReturnsNotFoundWhenUserDoesNotExist() {
        UserCommandService userCommandService = mock(UserCommandService.class);
        AuthenticationController controller = new AuthenticationController(userCommandService);

        // Simula credenciales incorrectas
        SignInResource resource = new SignInResource("wrong@example.com", "invalidPassword");

        // Simula que no se encontró ningún usuario con esas credenciales
        when(userCommandService.handle(any(SignInCommand.class))).thenReturn(Optional.empty());

        ResponseEntity<AuthenticatedUserResource> response = controller.signIn(resource);

        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
    }
}
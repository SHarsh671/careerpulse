package com.portfolio.jobapp.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final HttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");

    @Test
    void mapsBadRequestTo400() {
        var response = handler.handleBadRequestException(new BadRequestException("bad input"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("bad input");
    }

    @Test
    void mapsMissingResourcesTo404() {
        var response = handler.handleResourceNotFoundException(new ResourceNotFoundException("Company", "id", 4L), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getPath()).isEqualTo("/api/test");
    }

    @Test
    void mapsDuplicatesToConflict() {
        var response = handler.handleDuplicateResourceException(new DuplicateResourceException("duplicate"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void mapsUnauthorizedDomainExceptionsToForbidden() {
        var response = handler.handleUnauthorizedException(new UnauthorizedException("denied"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void mapsInvalidCredentialsToGenericUnauthorizedResponse() {
        var response = handler.handleBadCredentialsException(new BadCredentialsException("secret detail"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid email or password");
    }

    @Test
    void mapsAccessDeniedToForbidden() {
        var response = handler.handleAccessDeniedException(new AccessDeniedException("detail"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void hidesUnexpectedExceptionDetailsFromClient() {
        var response = handler.handleGlobalException(new IllegalStateException("database password leaked"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).doesNotContain("database password");
    }
}

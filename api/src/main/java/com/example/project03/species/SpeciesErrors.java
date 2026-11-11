package com.example.project03.species;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

/** Turns species errors into the RFC 9457 Problem Details responses described in docs/problemDetail.md. */
@RestControllerAdvice(assignableTypes = SpeciesController.class)
public class SpeciesErrors {
    /**
     * Builds the 404 response for an unknown species.
     *
     * @param error the exception thrown by {@link SpeciesService#get(String)}
     * @param request the current request, used for the {@code instance} field
     * @return a Problem Details body with type {@code /problems/not-found}
     */
    @ExceptionHandler(SpeciesNotFoundException.class)
    ProblemDetail notFound(SpeciesNotFoundException error, HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, error.getMessage());
        detail.setType(URI.create("/problems/not-found"));
        detail.setTitle("Not found");
        detail.setInstance(URI.create(request.getRequestURI()));
        return detail;
    }
}

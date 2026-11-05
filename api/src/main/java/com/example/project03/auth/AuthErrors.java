package com.example.project03.auth;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthErrors {
    @ExceptionHandler(ResponseStatusException.class)
    ProblemDetail authError(ResponseStatusException error) {
        var detail = ProblemDetail.forStatusAndDetail(error.getStatusCode(), error.getReason());
        detail.setType(java.net.URI.create("/problems/" + (error.getStatusCode().value() == 401 ? "unauthorized" : "auth-error")));
        return detail;
    }
}

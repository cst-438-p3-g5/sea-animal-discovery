package com.example.project03.sighting;

import java.net.URI;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.example.project03.species.SpeciesRepository;

/** Authenticated endpoint for creating a user's sighting. */
@RestController
public class SightingController {
    private final SightingRepository sightings;
    private final SpeciesRepository species;

    public SightingController(SightingRepository sightings, SpeciesRepository species) {
        this.sightings = sightings;
        this.species = species;
    }

    @PostMapping("/sightings")
    public ResponseEntity<SightingRepository.Sighting> create(
            @AuthenticationPrincipal Jwt jwt, @RequestBody CreateSightingRequest request) {
        if (request == null || request.species_id() == null || request.species_id().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "species_id is required.");
        }
        if (request.sighting_date() == null || request.sighting_date().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sighting_date is required.");
        }
        final LocalDate date;
        try {
            date = LocalDate.parse(request.sighting_date());
        } catch (RuntimeException error) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sighting_date must use YYYY-MM-DD format.");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sighting_date cannot be in the future.");
        }
        if (request.notes() != null && request.notes().length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "notes must be 500 characters or fewer.");
        }
        if (species.findById(request.species_id()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The selected species does not exist.");
        }

        var saved = sightings.create(jwt.getSubject(), request.species_id(), date, request.notes());
        return ResponseEntity.created(URI.create("/sightings/" + saved.id())).body(saved);
    }

    public record CreateSightingRequest(String species_id, String sighting_date, String notes) {}
}

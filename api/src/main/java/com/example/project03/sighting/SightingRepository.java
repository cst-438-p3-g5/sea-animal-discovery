package com.example.project03.sighting;

import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Persists sightings linked to an app user and a catalog species. */
@Repository
public class SightingRepository {
    private final JdbcClient jdbc;

    public SightingRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Sighting create(String userId, String speciesId, LocalDate date, String notes) {
        String id = UUID.randomUUID().toString();
        jdbc.sql("""
                INSERT INTO sighting (id, user_id, species_id, sighting_date, notes)
                VALUES (:id, :user, :species, :date, :notes)
                """).param("id", id).param("user", userId).param("species", speciesId)
                .param("date", Date.valueOf(date)).param("notes", notes).update();
        return new Sighting(id, userId, speciesId, date, notes);
    }

    /** JSON names match docs/openapi.yaml. */
    public record Sighting(String id, String user_id, String species_id,
                           LocalDate sighting_date, String notes) {}
}

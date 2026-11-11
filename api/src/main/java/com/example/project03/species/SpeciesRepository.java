package com.example.project03.species;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Reads the species catalog from the {@code species} table (see schema.sql and data.sql). */
@Repository
public class SpeciesRepository {
    private static final String COLUMNS = "id, common_name, identification_details, habitat, conservation_information";

    private final JdbcClient jdbc;

    /**
     * Creates the repository.
     *
     * @param jdbc Spring's JDBC client for the application database
     */
    public SpeciesRepository(JdbcClient jdbc) { this.jdbc = jdbc; }

    /**
     * Returns every species, sorted by common name.
     *
     * @return the whole catalog, or an empty list if it has no species
     */
    public List<Species> findAll() {
        return jdbc.sql("SELECT " + COLUMNS + " FROM species ORDER BY common_name")
                .query(Species.class).list();
    }

    /**
     * Looks up one species by id.
     *
     * @param id the species id, such as {@code sp-001}
     * @return the species, or empty if no species has that id
     */
    public Optional<Species> findById(String id) {
        return jdbc.sql("SELECT " + COLUMNS + " FROM species WHERE id = :id").param("id", id)
                .query(Species.class).optional();
    }
}

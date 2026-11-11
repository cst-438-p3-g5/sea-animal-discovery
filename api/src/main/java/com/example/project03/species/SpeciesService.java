package com.example.project03.species;

import java.util.List;
import org.springframework.stereotype.Service;

/** Business logic for browsing the species catalog (PRD-4). */
@Service
public class SpeciesService {
    private final SpeciesRepository species;

    /**
     * Creates the service.
     *
     * @param species where species are stored
     */
    public SpeciesService(SpeciesRepository species) { this.species = species; }

    /**
     * Lists the whole catalog.
     *
     * @return every species, sorted by common name
     */
    public List<Species> list() {
        return species.findAll();
    }

    /**
     * Gets one species profile.
     *
     * @param id the species id, such as {@code sp-001}
     * @return the species with that id
     * @throws SpeciesNotFoundException if no species has that id
     */
    public Species get(String id) {
        return species.findById(id).orElseThrow(() -> new SpeciesNotFoundException(id));
    }
}

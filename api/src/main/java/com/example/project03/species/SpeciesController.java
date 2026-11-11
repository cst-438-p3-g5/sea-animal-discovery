package com.example.project03.species;

import java.util.List;
import org.springframework.web.bind.annotation.*;

/** Public species catalog endpoints from docs/openapi.yaml (PRD-4). No sign-in is needed; see AuthConfig. */
@RestController
@RequestMapping("/species")
public class SpeciesController {
    private final SpeciesService species;

    /**
     * Creates the controller.
     *
     * @param species the species service
     */
    public SpeciesController(SpeciesService species) { this.species = species; }

    /**
     * {@code GET /species}: lists the species catalog.
     *
     * @return every species, sorted by common name
     */
    @GetMapping
    public List<Species> list() {
        return species.list();
    }

    /**
     * {@code GET /species/{id}}: gets one species profile.
     *
     * @param id the species id, such as {@code sp-001}
     * @return the species profile; an unknown id becomes a 404 Problem Details response
     */
    @GetMapping("/{id}")
    public Species get(@PathVariable("id") String id) {
        return species.get(id);
    }
}

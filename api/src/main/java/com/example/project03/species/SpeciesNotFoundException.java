package com.example.project03.species;

/** Thrown when a request asks for a species id that is not in the catalog. Mapped to a 404 by {@link SpeciesErrors}. */
public class SpeciesNotFoundException extends RuntimeException {
    /**
     * Creates the exception with the message used as the Problem Details {@code detail}.
     *
     * @param id the species id that was not found
     */
    public SpeciesNotFoundException(String id) {
        super("No species with id " + id + ".");
    }
}

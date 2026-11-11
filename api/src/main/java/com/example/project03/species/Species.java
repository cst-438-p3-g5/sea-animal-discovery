package com.example.project03.species;

/**
 * One species in the catalog (PRD-4). Field names match docs/erd.md and docs/openapi.yaml,
 * so the JSON uses snake_case.
 *
 * @param id stable id such as {@code sp-001}
 * @param common_name the name a beginner would recognize
 * @param identification_details how to recognize the animal
 * @param habitat where the animal lives
 * @param conservation_information its conservation status and how visitors should treat it
 */
public record Species(
        String id,
        String common_name,
        String identification_details,
        String habitat,
        String conservation_information) {}

package com.example.project03.species;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SpeciesServiceTests {
    static final Species STAR = new Species("sp-001", "Ochre sea star", "Five thick arms",
            "Rocky intertidal zones", "Recovering from sea star wasting disease");

    final SpeciesRepository repository = mock(SpeciesRepository.class);
    final SpeciesService service = new SpeciesService(repository);

    @Test void listReturnsTheWholeCatalog() {
        when(repository.findAll()).thenReturn(List.of(STAR));
        assertEquals(List.of(STAR), service.list());
    }

    @Test void listReturnsAnEmptyListWhenTheCatalogIsEmpty() {
        when(repository.findAll()).thenReturn(List.of());
        assertTrue(service.list().isEmpty());
    }

    @Test void getReturnsTheSpeciesWhenItExists() {
        when(repository.findById("sp-001")).thenReturn(Optional.of(STAR));
        assertEquals(STAR, service.get("sp-001"));
    }

    @Test void getRejectsAnUnknownIdWithNotFound() {
        when(repository.findById("sp-999")).thenReturn(Optional.empty());
        SpeciesNotFoundException error = assertThrows(SpeciesNotFoundException.class, () -> service.get("sp-999"));
        assertEquals("No species with id sp-999.", error.getMessage());
    }
}

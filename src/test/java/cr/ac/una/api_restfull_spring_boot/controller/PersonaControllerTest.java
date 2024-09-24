package cr.ac.una.api_restfull_spring_boot.controller;

import cr.ac.una.api_restfull_spring_boot.entity.Persona;
import cr.ac.una.api_restfull_spring_boot.repository.PersonaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PersonaControllerTest {

    @Mock
    private PersonaRepository personaRepository;

    @InjectMocks
    private PersonaController personaController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetPersonas() {
        List<Persona> mockPersonas = List.of(new Persona(1L,"Juan", 30), new Persona(2L,"Maria", 25));
        when(personaRepository.findAll()).thenReturn(mockPersonas);

        ResponseEntity<List<Persona>> response = personaController.getPersonas();
        assertEquals(mockPersonas, response.getBody());
    }

    @Test
    void testSavePersona() {
        Persona persona = new Persona(3L,"Ana", 20);
        when(personaRepository.save(persona)).thenReturn(persona);

        ResponseEntity<Persona> response = personaController.savePersona(persona);
        assertEquals(ResponseEntity.ok(persona), response);
    }

    @Test
    void testSavePersonaBadRequest() {
        Persona persona = new Persona(-1L,"", -1);

        ResponseEntity<Persona> response = personaController.savePersona(persona);
        assertEquals(ResponseEntity.badRequest().build(), response);
    }

    @Test
    void testUpdatePersona() {
        Persona existingPersona = new Persona(4L,"Carlos", 35);
        existingPersona.setId(1L);
        when(personaRepository.existsById(1L)).thenReturn(true);
        when(personaRepository.findById(1L)).thenReturn(Optional.of(existingPersona));
        when(personaRepository.save(existingPersona)).thenReturn(existingPersona);

        ResponseEntity<Persona> response = personaController.updatePersona(existingPersona);
        assertEquals(ResponseEntity.ok(existingPersona), response);
    }

    @Test
    void testUpdatePersonaNotFound() { // Check PersonaController.java line 49 to 51..
        Persona persona = new Persona(5L, "Laura", 28);
        persona.setId(1L);
        when(personaRepository.existsById(1L)).thenReturn(false);

        ResponseEntity<Persona> response = personaController.updatePersona(persona);
        assertEquals(ResponseEntity.badRequest().build(), response);
    }

    @Test
    void testDeletePersona() {
        Long id = 1L;
        when(personaRepository.existsById(id)).thenReturn(true);

        ResponseEntity<Void> response = personaController.deletePersona(id);
        assertEquals(ResponseEntity.ok().build(), response);
        verify(personaRepository).deleteById(id);
    }

    @Test
    void testDeletePersonaNotFound() {
        Long id = 1L;
        when(personaRepository.existsById(id)).thenReturn(false);

        ResponseEntity<Void> response = personaController.deletePersona(id);
        assertEquals(ResponseEntity.notFound().build(), response);
    }

    @Test
    void testGetPersona() {
        Long id = 1L;
        Persona persona = new Persona(6L, "Pedro", 40);
        persona.setId(id);
        when(personaRepository.findById(id)).thenReturn(Optional.of(persona));

        ResponseEntity<Persona> response = personaController.getPersona(id);
        assertEquals(ResponseEntity.ok(persona), response);
    }

    @Test
    void testGetPersonaNotFound() {
        Long id = 1L;
        when(personaRepository.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<Persona> response = personaController.getPersona(id);
        assertEquals(ResponseEntity.notFound().build(), response);
    }
}

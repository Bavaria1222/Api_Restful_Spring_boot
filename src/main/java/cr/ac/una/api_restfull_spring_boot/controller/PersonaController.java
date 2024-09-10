package cr.ac.una.api_restfull_spring_boot.controller;

import cr.ac.una.api_restfull_spring_boot.entity.Persona;
import cr.ac.una.api_restfull_spring_boot.exeption.PersonaNotFoundException;
import cr.ac.una.api_restfull_spring_boot.repository.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping({"/api"})
public class PersonaController {
    @Autowired
    PersonaRepository personaRepository;

    public  PersonaController () {

    }
    @GetMapping({"/persona"})
    ResponseEntity<List <Persona>> getAllPersona() {
      List<Persona> personas = personaRepository.findAll();
      if(personas.isEmpty()) {
          return ResponseEntity.noContent().build(); // 204 No content si no hay personas

      }
      return ResponseEntity.ok(personas); // 200k
    }

    // Obtener una persona por ID
    @GetMapping("/persona/{id}")
    public ResponseEntity<Persona> getPersona(@PathVariable Long id) {
        return personaRepository.findById(id)
                .map(ResponseEntity::ok)  // Devuelve 200 OK si la persona es encontrada
                .orElse(ResponseEntity.notFound().build());  // Devuelve 404 Not Found si no se encuentra
    }


    // Metodo para guardar una nueva persona
    @PostMapping("/persona")
    public ResponseEntity<Persona> savePersona(@RequestBody Persona persona) {

        // Validar si el nombre es vacío o nulo
        if (persona.getNombre() == null || persona.getNombre().isEmpty()) {
            return ResponseEntity.badRequest().body(null);  // 400 Bad Request si el nombre es inválido
        }

        // Validar si la edad es menor o igual a 0
        if (persona.getEdad() <= 0) {
            return ResponseEntity.badRequest().body(null);  // 400 Bad Request si la edad es inválida
        }

        Persona newPerson = personaRepository.save(persona);
        return ResponseEntity.ok(newPerson);

    }







}

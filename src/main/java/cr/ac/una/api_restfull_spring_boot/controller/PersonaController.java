package cr.ac.una.api_restfull_spring_boot.controller;

import cr.ac.una.api_restfull_spring_boot.entity.Persona;
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

    @GetMapping("/persona")
    ResponseEntity <List<Persona>> getPersonas(){
        return ResponseEntity.ok(personaRepository.findAll());
    }

    @PostMapping("/persona")
    public ResponseEntity<Persona> savePersona(@RequestBody Persona persona) {
        return Optional.ofNullable(persona)
                .filter(p -> p.getNombre() != null && !p.getNombre().isEmpty())  // Validar que el nombre no esté vacío
                .filter(p -> p.getEdad() != null && p.getEdad() >= 0)  // Validar que la edad no sea negativa
                .map(personaRepository::save)  // Guardar si pasa las validaciones
                .map(ResponseEntity::ok)  // Devolver ResponseEntity con el objeto Persona
                .orElseGet(() -> ResponseEntity.badRequest().build());  // Si falla, devolver un ResponseEntity vacío con BadRequest (400)
    }

    @PutMapping("/persona")
    public ResponseEntity<Persona> updatePersona(@RequestBody Persona persona) {
        return Optional.ofNullable(persona)
                .filter(p -> p.getId() != null && personaRepository.existsById(p.getId()))  // Verificar ID no nulo y si existe
                .filter(p -> p.getNombre() != null && !p.getNombre().isEmpty())  // Validar que el nombre no esté vacío
                .filter(p -> p.getEdad() != null && p.getEdad() >= 0)  // Validar que la edad no sea negativa
                .map(p -> {
                    // Encontrar la persona y actualizar los datos
                    Persona existingPersona = personaRepository.findById(p.getId()).orElseThrow();
                    existingPersona.setNombre(p.getNombre());
                    existingPersona.setEdad(p.getEdad());
                    return personaRepository.save(existingPersona);  // Guardar los cambios
                })
                .map(ResponseEntity::ok)  // Devolver 200 OK si todo está bien
                .orElseThrow(() -> new RuntimeException("Error interno al actualizar"));   // Para generar un error 500
                // Cambiar por
                // .orElseGet(() -> ResponseEntity.badRequest().build());
                //Para obtener un bad request en caso de error al actualizar.
    }

    @DeleteMapping("/persona/{id}")
    public ResponseEntity<Void> deletePersona(@PathVariable("id") Long id) {
        return Optional.ofNullable(id)
                .filter(personaRepository::existsById)  // Verificar si el ID existe en la base de datos
                .map(existingId -> {
                    personaRepository.deleteById(existingId);  // Si existe, eliminar la persona
                    return ResponseEntity.ok().<Void>build();  // Devolver 200 OK si se eliminó correctamente
                })
                .orElseGet(() -> ResponseEntity.notFound().build());  // Si no existe, devolver 404 Not Found
    }

    @GetMapping("/persona/{id}")
    public ResponseEntity<Persona> getPersona(@PathVariable("id") Long id) {
        return Optional.ofNullable(id)  // Asegura que el ID no sea nulo
                .flatMap(personaRepository::findById)  // Buscar la persona en la base de datos
                .map(ResponseEntity::ok)  // Si existe, devolver 200 OK con la persona encontrada
                .orElseGet(() -> ResponseEntity.notFound().build());  // Si no existe, devolver 404 Not Found
    }
}

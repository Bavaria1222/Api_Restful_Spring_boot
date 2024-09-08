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

    public  PersonaController () {

    }
    @GetMapping({"/persona"})
    ResponseEntity<List <Persona>> getPersona() {return ResponseEntity.ok(personaRepository.findAll());}

    @PostMapping({"/persona"})
    ResponseEntity <Persona> savePersona(@RequestBody Persona persona) {
        return ResponseEntity.ok(personaRepository.save(persona));
    }

    @PutMapping({"/persona"})
    ResponseEntity <Persona> updatePersona(@RequestBody Persona persona) {
        return ResponseEntity.ok((Persona)this.personaRepository.save(persona));
    }

    @DeleteMapping({"/persona/{id}"})
    ResponseEntity <Void> deletePersona(@PathVariable ("id") Long id) {
        this.personaRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping({"/persona/{id}"})
    ResponseEntity getPersona(@PathVariable("id") Long id) {
        Optional<Persona> optional = this.personaRepository.findById(id);
        return optional.map(ResponseEntity::ok).orElseGet(() -> {
            return ResponseEntity.notFound().build();
        });
    }

}

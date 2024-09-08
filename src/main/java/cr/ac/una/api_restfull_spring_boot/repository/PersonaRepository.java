package cr.ac.una.api_restfull_spring_boot.repository;

import cr.ac.una.api_restfull_spring_boot.entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonaRepository  extends JpaRepository<Persona, Long> {

}

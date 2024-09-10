package cr.ac.una.api_restfull_spring_boot.exeption;

public class PersonaNotFoundException extends RuntimeException {

    //Constructor que recibe el ID de la persona que no se encontró
    public PersonaNotFoundException(Long id) {

        super("Persona " + id + " not found");

    }
}

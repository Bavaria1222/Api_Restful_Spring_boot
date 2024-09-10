package cr.ac.una.api_restfull_spring_boot.exeption;


// Excepción personalizada para manejar solicitudes incorrectas (Bad Request)
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}

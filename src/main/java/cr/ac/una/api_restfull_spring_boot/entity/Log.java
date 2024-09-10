package cr.ac.una.api_restfull_spring_boot.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
/*Loombok*/
@Data   //Setters, getters, toStrins ... etc
@NoArgsConstructor //Constructor vacio
@AllArgsConstructor // Genera un constructor con todos los argumentos
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Identificador del log
    private String endpointName;  // Nombre del endpoint o metodo
    private String httpMethod;  // Metodo HTTP (GET, POST, etc.)
    private int httpCode;  // Código de respuesta HTTP
    private String httpStatusCode;  // Tipo de error (si aplica)
    private Long responseTime;  // Tiempo de respuesta en milisegundos
    private LocalDateTime timestamp;  // Fecha y hora del evento

}

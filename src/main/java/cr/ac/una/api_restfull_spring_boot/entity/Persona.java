package cr.ac.una.api_restfull_spring_boot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
/*Loombok*/
@Data     //Genera getters,setters,toString, equals y hashCode
@NoArgsConstructor //Constructor vacio
@AllArgsConstructor // Genera un constructor con todos los argumentos


public class Persona {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private String nombre;
    private int edad;

}

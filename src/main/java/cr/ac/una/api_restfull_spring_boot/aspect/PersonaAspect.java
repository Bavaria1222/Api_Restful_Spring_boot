package cr.ac.una.api_restfull_spring_boot.aspect;

import cr.ac.una.api_restfull_spring_boot.entity.Log;
import cr.ac.una.api_restfull_spring_boot.repository.LogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.logging.Logger;

@Aspect
@Component
public class PersonaAspect {

    private static final Logger logger = Logger.getLogger(PersonaAspect.class.getName());
    private final LogRepository logRepository;
    @Autowired
    private HttpServletRequest request; // Inyectar HttpServletRequest para obtener el nombre del enpoint

    public PersonaAspect(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    //Interceptar todos los metodos de la clase PersonaController que devuelven ResponseEntity
    @Around("execution(* cr.ac.una.api_restfull_spring_boot.controller.PersonaController.*(..))")
    public Object logPersona (ProceedingJoinPoint joinPoint) throws Throwable {

        // Capturar el tiempo de inicio
        long startTime = System.currentTimeMillis();

        // Ejecutar el metodo original del controlador
        Object result = joinPoint.proceed();

        // Capturar el tiempo de fin
        long endTime = System.currentTimeMillis();

        // Calcular el tiempo de respuesta en milisegundos
        long responseTime = endTime - startTime;


        //Verifica si la respuesta es una instancia de Response Entity
        if(result instanceof ResponseEntity){
            ResponseEntity<?> responseEntity = (ResponseEntity<?>) result;

            // Crear un nuevo log
            Log log = new Log();

           //1. Obtener el nombre del enpoint ejecutado

            log.setEndpointName(joinPoint.getSignature().getName());

           //2. Obtener el metodo HTTP (GET, POST, etc.)
            log.setHttpMethod(request.getMethod());

           //3.Obtenener la respueta HTTP
//            log.setHttpCode(responseEntity.getStatusCodeValue());
            int httpResponse = responseEntity.getStatusCodeValue();
            log.setHttpCode(httpResponse);


            //4. Obtener el detalle del código de respuesta (por ejemplo, BAD_REQUEST)
            HttpStatus status = HttpStatus.valueOf(httpResponse);
            log.setHttpStatusCode(status.getReasonPhrase());

            // 5. Guardar el tiempo de respuesta en milisegundos
            log.setResponseTime(responseTime);

            //6 timestamp
            log.setTimestamp(LocalDateTime.now());

            //Guardar el log en la base de datos
            logRepository.save(log);

        }
        return result;
    }

}

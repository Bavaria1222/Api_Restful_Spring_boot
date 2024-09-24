package cr.ac.una.api_restfull_spring_boot.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.logging.Logger;

@Aspect
@Component
public class PersonaAspect {

    private static final Logger logger = Logger.getLogger(PersonaAspect.class.getName());

    @Before("execution(* cr.ac.una.api_restfull_spring_boot.controller.PersonaController.*(..))")
    public void logAfterGetName(JoinPoint joinPoint) {
        logger.info("Enpoint_ejecutado: " + joinPoint.getSignature().getName());
    }

    @Around("execution(* cr.ac.una.api_restfull_spring_boot.controller.PersonaController.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        // Obtener el tiempo inicial en milisegundos
        long start = System.currentTimeMillis();

        // Obtener el método HTTP actual
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String metodoHTTP = request.getMethod();  // Obtener el método HTTP (GET, POST, PUT, DELETE)

        // Ejecutar el método original
        Object proceed = joinPoint.proceed();

        // Calcular el tiempo de ejecución
        long executionTime = System.currentTimeMillis() - start;

        // Registrar el método HTTP, el endpoint y el tiempo de ejecución en los logs
        logger.info("Enpoint_ejecutado: " + joinPoint.getSignature().getName() + " - Método HTTP: " + metodoHTTP + " - Tiempo de ejecución: " + executionTime + " ms");

        return proceed;
    }
}

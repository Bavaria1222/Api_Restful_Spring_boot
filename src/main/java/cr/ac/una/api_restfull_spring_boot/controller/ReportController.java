package cr.ac.una.api_restfull_spring_boot.controller;

import cr.ac.una.api_restfull_spring_boot.service.LogProccesor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/log")
public class ReportController {

    private final LogProccesor logProccesor;

    // Inyección de dependencia a través del constructor
    public ReportController(LogProccesor logProccesor) {
        this.logProccesor = logProccesor;
    }

    // Endpoint para generar logs
    @GetMapping("/generarLogs")
    public void generateLogs() {
        logProccesor.reloadLogs();
    }

    // Endpoint para obtener el reporte de errores por tipo
    @GetMapping("/ObtenerReporteErrores")
    public Map<String, Long> getErrorReport() {
        return logProccesor.getErrorReportByType();  // Devuelve el reporte de errores agrupado por tipo
    }

    // Endpoint para obtener el top 3 de errores más frecuentes
    @GetMapping("/errores/masFrecuente")
    public List<Map.Entry<String, Long>> getMostFrequentErrors() {
        return logProccesor.getErroresFrecuentes();  // Devuelve el top 3 de errores más frecuentes
    }

    // Endpoint para obtener la hora con más errores
    @GetMapping("/errores/horaPico")
    public ResponseEntity<Map<String, String>> getPeakHour() {
        Map<String, String> peakHour = logProccesor.getHoraPicoConErrores();
        if (peakHour != null) {
            return ResponseEntity.ok(peakHour);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    // Endpoint para obtener el reporte de tiempos de respuesta
    @GetMapping("/enpoint/tiempoRespuesta")
    public Map<String, String> getResponseTimes() {
        return logProccesor.generarReporteTiemposDeRespuesta();  // Genera el reporte de tiempos de respuesta (promedio, mínimo, máximo, mediana)
    }

    // Endpoint para obtener la distribución de tiempos de respuesta por endpoint
    @GetMapping("/enpoint/distribucion")
    public Map<String, Map<String, Long>> getResponseTimeDistribution() {
        return logProccesor.generarDistribucionDeTiemposDeRespuesta();  // Devuelve la distribución de tiempos de respuesta por endpoint
    }

    @GetMapping("/enpoint/SolicitudesLentas")
    public ResponseEntity<List<Map.Entry<String, Long>>> getSlowRequests(@RequestParam(defaultValue = "500") long umbral) {
        if (umbral <= 0) {
            return ResponseEntity.badRequest().body(List.of());  // Retorna un error si el umbral no es válido
        }
        return ResponseEntity.ok(logProccesor.detectarSolicitudesLentas(umbral));  // Detectar solicitudes lentas que superan el umbral
    }

    // Endpoint para obtener el top 3 de endpoints más utilizados
    @GetMapping("/enpoint/top-3-utilizados")
    public List<Map.Entry<String, Long>> getTop3UsedEndpoints() {
        return logProccesor.getTop3EndpointsMasUtilizados();  // Devuelve el top 3 de endpoints más utilizados
    }

    // Endpoint para obtener el top 3 de endpoints menos utilizados
    @GetMapping("/enpoint/top-3-MenosUtilizados")
    public List<Map.Entry<String, Long>> getTop3LeastUsedEndpoints() {
        return logProccesor.getTop3EndpointsMenosUtilizados();  // Devuelve el top 3 de endpoints menos utilizados
    }

    // Endpoint para obtener el conteo de peticiones por endpoint y por método HTTP (GET, POST, PUT, DELETE)
    @GetMapping("/enpoint/Contador")
    public Map<String, Map<String, Long>> getConteoPeticionesPorMetodo() {
        return logProccesor.conteoPeticionesPorEndpointYMetodo();  // Devuelve el conteo de peticiones por endpoint y por método HTTP
    }
}

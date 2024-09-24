package cr.ac.una.api_restfull_spring_boot.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@Data
@Service
public class LogProccesor {

    private List<String> logs;
    private List<String > errorLogs;
    private List<String> enpointsInformation;

    // Metodo que lee el archivo y almacena las líneas en una lista
    LogProccesor() {
        this.logs = readLogFile();
        this.errorLogs = filterErrorLogs();
        this.enpointsInformation = filterEnpointsInformation();
    }

    public List<String> readLogFile() {
        // Ruta al archivo log
        Path path = Paths.get("app.log");

        try (Stream<String> lines = Files.lines(path)) {
            return lines
                    .filter(line -> line.contains("Api_Restfull_Spring_boot"))  // Filtra solo las líneas que contienen "Api_Restfull_Spring_boot"
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }


    // Metodo que filtra los errores y los almacena en errorLogs
    private List<String> filterErrorLogs() {
        return logs.stream()
                .filter(line ->
                        (line.contains("DispatcherServlet") &&
                                (line.matches(".*Completed 40[0-9].*") || line.contains("status 500")))  // Incluye errores 40x y 500
                                || line.contains(": Application run failed")  // Condición para "Application run failed"
                )
                .collect(Collectors.toList());
    }

    private List<String> filterEnpointsInformation() {
        return logs.stream()
                .filter(line -> line.contains("PersonaAspect") && line.trim().endsWith("ms"))  // Filtrar las líneas que contienen "PersonaAspect" y terminan con "ms"
                .collect(Collectors.toList());
    }

    // Método para extraer el tipo de error
    public String extractErrorType(String line) {
        if (line.contains("404")) {
            return "404 NOT_FOUND";
        } else if (line.contains("400")) {
            return "400 BAD_REQUEST";
        } else if (line.contains("500")) {
            return "500 INTERNAL_SERVER_ERROR";
        } else if (line.contains("405")) {
            return "405 METHOD_NOT_ALLOWED";
        } else if (line.contains("Application run failed")) {
            return "Application run failed";
        }
        return "Unknown Error";
    }

    // Agrupa los errores por tipo utilizando errorLogs procesado
    public Map<String, Long> getErrorReportByType() {
        return errorLogs.stream()
                .collect(Collectors.groupingBy(this::extractErrorType, Collectors.counting()));
    }

    // Método para obtener el top 3 errores más frecuentes
    public List<Map.Entry<String, Long>> getErroresFrecuentes() {
        return getErrorReportByType().entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(3)
                .collect(Collectors.toList());
    }

    public void reloadLogs() {
        this.logs = readLogFile();  // Vuelve a cargar los logs
        this.errorLogs = filterErrorLogs();  // Vuelve a filtrar y cargar los errores
        this.enpointsInformation = filterEnpointsInformation(); // Vuelve a filtrar y cargar la información de los endpoints utilizados
    }

    /* Reporte de horas pico errores */
    public Map<String, Long> agruparErroresPorHora() {
        DateTimeFormatter formato = DateTimeFormatter.ISO_OFFSET_DATE_TIME;  // Usar ISO_OFFSET_DATE_TIME para timestamps con zona horaria

        // Agrupar los errores por hora
        return errorLogs.stream()
                .map(line -> {
                    try {
                        if (line.length() >= 29) {  // Asegurarse de que la línea tenga suficiente longitud para el timestamp
                            String timestamp = line.substring(0, 29);  // Capturar los primeros 29 caracteres del timestamp
                            OffsetDateTime dateTime = OffsetDateTime.parse(timestamp, formato);  // Analizar el timestamp
                            return String.format("%02d", dateTime.getHour());  // Extraer la hora en formato "00", "01", etc.
                        }
                    } catch (Exception e) {
                        System.err.println("Error al procesar la línea: " + line + " - " + e.getMessage());
                    }
                    return null;  // Si hay un error o la línea no tiene el formato adecuado, devolver null
                })
                .filter(hour -> hour != null)  // Filtrar las líneas que no pudieron ser procesadas
                .collect(Collectors.groupingBy(hour -> hour, Collectors.counting()));  // Agrupar por hora y contar los errores
    }

    public Map<String, String> getHoraPicoConErrores() {
        // Encuentra la hora con más errores
        Map.Entry<String, Long> peakHourEntry = agruparErroresPorHora().entrySet().stream()
                .max(Map.Entry.<String, Long>comparingByValue())  // Encuentra la entrada con la mayor cantidad de errores
                .orElse(null);  // Si no hay errores, devuelve null

        // Verifica si se encontró una hora pico
        if (peakHourEntry != null) {
            // Crear un mapa con las claves y valores personalizados
            Map<String, String> response = new LinkedHashMap<>();
            response.put("Hora pico errores", peakHourEntry.getKey());  // Clave personalizada para la hora
            response.put("Cantidad de errores", String.valueOf(peakHourEntry.getValue()));  // Clave personalizada para la cantidad de errores
            return response;
        } else {
            return null;  // Si no hay errores, devolver null
        }
    }

    /* Reporte de Tiempos de Respuesta */
    public List<Long> getTiemposDeRespuestaEnpoints() {
        return enpointsInformation.stream()
                .filter(line -> line.contains("Tiempo de ejecución"))  // Filtra las líneas que contienen "Tiempo de ejecución"
                .map(line -> {
                    try {
                        // Buscar el índice donde aparece "Tiempo de ejecución:"
                        int startIndex = line.indexOf("Tiempo de ejecución:") + "Tiempo de ejecución:".length();
                        int endIndex = line.indexOf(" ms", startIndex);  // Buscar el índice donde termina la medida de tiempo en "ms"

                        // Validar si los índices obtenidos son correctos
                        if (startIndex > 0 && endIndex > startIndex) {
                            // Extraer la subcadena que contiene el número del tiempo
                            String tiempoStr = line.substring(startIndex, endIndex).trim();
                            return Long.parseLong(tiempoStr);  // Convertir a long
                        } else {
                            throw new StringIndexOutOfBoundsException("Índices no válidos para la línea: " + line);
                        }
                    } catch (Exception e) {
                        System.err.println("Error al procesar la línea: " + line + " - " + e.getMessage());
                    }
                    return null;  // Si no se puede extraer el tiempo, devolver null
                })
                .filter(Objects::nonNull)  // Filtrar los valores nulos
                .collect(Collectors.toList());  // Devolver la lista de tiempos de respuesta
    }


    // Método para calcular promedio, mínimo, máximo y media de los tiempos de respuesta
    public Map<String, String> generarReporteTiemposDeRespuesta() {
        List<Long> tiempos = getTiemposDeRespuestaEnpoints();

        // Ordenar los tiempos
        List<Long> tiemposOrdenados = tiempos.stream().sorted().collect(Collectors.toList());

        // Calcular el promedio
        double promedio = tiempos.stream().mapToLong(val -> val).average().orElse(0.0);

        // Calcular el mínimo
        long minimo = tiempos.stream().mapToLong(val -> val).min().orElse(0L);

        // Calcular el máximo
        long maximo = tiempos.stream().mapToLong(val -> val).max().orElse(0L);

        // Calcular la mediana
        double mediana;
        int size = tiemposOrdenados.size();
        if (size % 2 == 0) {
            mediana = (tiemposOrdenados.get(size / 2 - 1) + tiemposOrdenados.get(size / 2)) / 2.0;
        } else {
            mediana = tiemposOrdenados.get(size / 2);
        }

        // Devolver los resultados
        Map<String, String> reporte = new HashMap<>();
        reporte.put("promedio", promedio + " ms");
        reporte.put("mínimo", minimo + " ms");
        reporte.put("máximo", maximo + " ms");
        reporte.put("mediana", mediana + " ms");

        return reporte;
    }

    // Distribución de tiempos de respuesta por endpoint
    // Distribución de tiempos de respuesta por endpoint
    public Map<String, List<Long>> getTiemposDeRespuestaPorEndpoint() {
        return enpointsInformation.stream()
                .map(line -> {
                    try {
                        if (line.contains("Enpoint_ejecutado") && line.contains("Tiempo de ejecución")) {
                            // Separar el nombre del endpoint y el tiempo de ejecución
                            String[] partes = line.split(": Enpoint_ejecutado: ");
                            if (partes.length > 1) {
                                String[] metodoYTiempo = partes[1].split(" - Tiempo de ejecución: ");
                                if (metodoYTiempo.length > 1) {
                                    String metodo = metodoYTiempo[0].trim();
                                    String tiempoString = metodoYTiempo[1].replace(" ms", "").trim();
                                    long tiempo = Long.parseLong(tiempoString);  // Convertir el tiempo a long
                                    return new AbstractMap.SimpleEntry<>(metodo, tiempo);
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error al procesar la línea: " + line + " - " + e.getMessage());
                    }
                    return null;
                })
                .filter(Objects::nonNull)  // Filtrar los valores nulos
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,  // Agrupar por el nombre del método (endpoint)
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())  // Crear una lista de los tiempos de respuesta
                ));
    }


    public Map<String, Map<String, Long>> generarDistribucionDeTiemposDeRespuesta() {
        // Obtener los tiempos de respuesta agrupados por endpoint
        Map<String, List<Long>> tiemposPorEndpoint = getTiemposDeRespuestaPorEndpoint();

        // Calcular las estadísticas (mínimo, máximo, promedio) por cada endpoint
        return tiemposPorEndpoint.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,  // Nombre del endpoint
                        entry -> {
                            List<Long> tiempos = entry.getValue();

                            // Calcular mínimo, máximo y promedio
                            long min = tiempos.stream().min(Long::compare).orElse(0L);
                            long max = tiempos.stream().max(Long::compare).orElse(0L);
                            long sum = tiempos.stream().mapToLong(Long::longValue).sum();
                            long promedio = tiempos.isEmpty() ? 0 : sum / tiempos.size();  // Evitar división por cero

                            // Crear un mapa con las estadísticas
                            Map<String, Long> estadisticas = new HashMap<>();
                            estadisticas.put("mínimo", min);
                            estadisticas.put("máximo", max);
                            estadisticas.put("promedio", promedio);

                            return estadisticas;  // Devolver las estadísticas por cada endpoint
                        }
                ));
    }




    //  Detección de solicitudes lentas (outliers) y su frecuencia
    public List<Map.Entry<String, Long>> detectarSolicitudesLentas(long umbral) {
        return getTiemposDeRespuestaPorEndpoint().entrySet().stream()
                .flatMap(entry -> {
                    String endpoint = entry.getKey();
                    List<Long> tiempos = entry.getValue();
                    return tiempos.stream()
                            .filter(tiempo -> tiempo > umbral)  // Filtra solo las solicitudes que superan el umbral
                            .map(tiempo -> new AbstractMap.SimpleEntry<>(endpoint, tiempo));  // Crea una nueva entrada (endpoint, tiempo)
                })
                .collect(Collectors.toList());
    }

    public void generarReporteSolicitudesLentas() {
        long umbral = 500;  // Definir el umbral, por ejemplo, 500 ms

        List<Map.Entry<String, Long>> solicitudesLentas = detectarSolicitudesLentas(umbral);

        if (solicitudesLentas.isEmpty()) {
            System.out.println("No se encontraron solicitudes lentas.");
        } else {
            System.out.println("Solicitudes lentas (superiores a " + umbral + " ms):");
            solicitudesLentas.forEach(entry ->
                    System.out.println("Endpoint: " + entry.getKey() + " - Tiempo: " + entry.getValue() + " ms"));
        }
    }

    // 3. Reporte de Uso de Endpoints (respuesta en json)
    public List<Map.Entry<String, Long>> getTop3EndpointsMasUtilizados() {
        return enpointsInformation.stream()
                .filter(line -> line.contains("Enpoint_ejecutado"))  // Filtrar las líneas que contienen "Enpoint_ejecutado"
                .map(this::extraerNombreEndpoint)  // Extraer el nombre del endpoint
                .collect(Collectors.groupingBy(endpoint -> endpoint, Collectors.counting()))  // Agrupar por nombre de endpoint y contar ocurrencias
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))  // Ordenar por cantidad de usos (de mayor a menor)
                .limit(3)  // Limitar a los 3 más utilizados
                .collect(Collectors.toList());
    }


    // Reporte de Endpoints menos utilizados (respuesta en json)
    public List<Map.Entry<String, Long>> getTop3EndpointsMenosUtilizados() {
        return enpointsInformation.stream()
                .filter(line -> line.contains("Enpoint_ejecutado"))  // Filtrar las líneas que contienen "Enpoint_ejecutado"
                .map(this::extraerNombreEndpoint)  // Extraer el nombre del endpoint
                .collect(Collectors.groupingBy(endpoint -> endpoint, Collectors.counting()))  // Agrupar por nombre de endpoint y contar ocurrencias
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue())  // Ordenar por cantidad de usos (de menor a mayor)
                .limit(3)  // Limitar a los 3 menos utilizados
                .collect(Collectors.toList());
    }



    //  Conteo de peticiones por endpoint y por método HTTP (GET, POST, PUT, DELETE)
    public Map<String, Map<String, Long>> conteoPeticionesPorEndpointYMetodo() {
        return enpointsInformation.stream()
                .collect(Collectors.groupingBy(
                        this::extraerNombreEndpoint,  // Agrupa por nombre del endpoint
                        Collectors.groupingBy(
                                this::extraerMetodoHTTP,  // Agrupa dentro de cada endpoint por el método HTTP
                                Collectors.counting()  // Cuenta la cantidad de peticiones por método
                        )
                ));
    }


    private String extraerNombreEndpoint(String lineaLog) {
        // Caso 1: Buscar si la línea contiene "Enpoint_ejecutado:"
        if (lineaLog.contains("Enpoint_ejecutado:")) {
            int startIndex = lineaLog.indexOf("Enpoint_ejecutado:") + "Enpoint_ejecutado:".length();
            int endIndex = lineaLog.indexOf(" -", startIndex);  // Busca hasta el siguiente " -"

            // Si no se encuentra el final esperado, tomar hasta el final de la línea
            if (endIndex == -1) {
                endIndex = lineaLog.length();
            }

            String nombreEndpoint = lineaLog.substring(startIndex, endIndex).trim();
            System.out.println("Nombre del endpoint extraído: " + nombreEndpoint);  // Depuración
            return nombreEndpoint;
        }

        // Caso 2: Buscar otro formato de línea si "Enpoint_ejecutado:" no está presente
        if (lineaLog.contains("El método")) {
            int startIndex = lineaLog.indexOf("El método") + "El método".length();
            int endIndex = lineaLog.indexOf(" ", startIndex);  // Extrae el nombre del método

            if (endIndex == -1) {
                endIndex = lineaLog.length();
            }

            String nombreMetodo = lineaLog.substring(startIndex, endIndex).trim();
            System.out.println("Nombre del método extraído: " + nombreMetodo);  // Depuración
            return nombreMetodo;
        }

        // Si la línea no contiene información de endpoint o método, marcar como "Desconocido"
        System.out.println("No se pudo extraer el nombre del endpoint o método, devolviendo 'Desconocido'");  // Depuración
        return "Desconocido";
    }







    private String extraerMetodoHTTP(String lineaLog) {
        if (lineaLog.contains("Método HTTP:")) {
            int startIndex = lineaLog.indexOf("Método HTTP:") + "Método HTTP:".length();
            int endIndex = lineaLog.indexOf(" -", startIndex);

            // Si no se encuentra " -", tomar hasta el final de la línea
            if (endIndex == -1) {
                endIndex = lineaLog.length();
            }

            String metodoHTTP = lineaLog.substring(startIndex, endIndex).trim();
            System.out.println("Método HTTP extraído: " + metodoHTTP);  // Depuración
            return metodoHTTP;
        }

        System.out.println("No se pudo extraer el método HTTP, devolviendo 'UNKNOWN'");  // Depuración
        return "UNKNOWN";
    }








}

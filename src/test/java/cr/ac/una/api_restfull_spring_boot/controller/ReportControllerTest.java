package cr.ac.una.api_restfull_spring_boot.controller;

import cr.ac.una.api_restfull_spring_boot.service.LogProccesor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportControllerTest {

    @Mock
    private LogProccesor logProccesor;

    @InjectMocks
    private ReportController reportController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateLogs() {
        reportController.generateLogs();
        verify(logProccesor).reloadLogs();
    }

    @Test
    void testGetErrorReport() {
        testWithMockedReturn(
                () -> Map.of("ErrorType1", 10L, "ErrorType2", 5L),
                logProccesor::getErrorReportByType,
                reportController::getErrorReport
        );
    }

    @Test
    void testGetMostFrequentErrors() {
        testWithMockedReturn(
                () -> List.of(
                        Map.entry("Error1", 20L),
                        Map.entry("Error2", 15L),
                        Map.entry("Error3", 10L)
                ),
                logProccesor::getErroresFrecuentes,
                reportController::getMostFrequentErrors
        );
    }

    @Test
    void testGetPeakHour() {
        Map<String, String> mockPeakHour = Map.of("peakHour", "14:00");
        when(logProccesor.getHoraPicoConErrores()).thenReturn(mockPeakHour);

        assertEquals(ResponseEntity.ok(mockPeakHour), reportController.getPeakHour());
    }

    @Test
    void testGetResponseTimes() {
        testWithMockedReturn(
                () -> Map.of("average", "100ms", "max", "500ms"),
                logProccesor::generarReporteTiemposDeRespuesta,
                reportController::getResponseTimes
        );
    }

    @Test
    void testGetResponseTimeDistribution() {
        testWithMockedReturn(
                () -> Map.of(
                        "endpoint1", Map.of("average", 100L),
                        "endpoint2", Map.of("average", 200L)
                ),
                logProccesor::generarDistribucionDeTiemposDeRespuesta,
                reportController::getResponseTimeDistribution
        );
    }

    @Test
    void testGetSlowRequests() {
        List<Map.Entry<String, Long>> mockSlowRequests = List.of(
                Map.entry("endpoint1", 600L),
                Map.entry("endpoint2", 700L)
        );
        when(logProccesor.detectarSolicitudesLentas(500)).thenReturn(mockSlowRequests);

        ResponseEntity<List<Map.Entry<String, Long>>> response = reportController.getSlowRequests(500);
        assertEquals(ResponseEntity.ok(mockSlowRequests), response);
    }

    @Test
    void testGetSlowRequestsBadRequest() {
        assertEquals(ResponseEntity.badRequest().body(List.of()), reportController.getSlowRequests(-1));
    }

    @Test
    void testGetTop3UsedEndpoints() {
        testWithMockedReturn(
                () -> List.of(
                        Map.entry("endpoint1", 100L),
                        Map.entry("endpoint2", 90L),
                        Map.entry("endpoint3", 80L)
                ),
                logProccesor::getTop3EndpointsMasUtilizados,
                reportController::getTop3UsedEndpoints
        );
    }

    @Test
    void testGetTop3LeastUsedEndpoints() {
        testWithMockedReturn(
                () -> List.of(
                        Map.entry("endpoint1", 10L),
                        Map.entry("endpoint2", 5L),
                        Map.entry("endpoint3", 2L)
                ),
                logProccesor::getTop3EndpointsMenosUtilizados,
                reportController::getTop3LeastUsedEndpoints
        );
    }

    @Test
    void testGetConteoPeticionesPorMetodo() {
        testWithMockedReturn(
                () -> Map.of(
                        "endpoint1", Map.of("GET", 20L, "POST", 15L),
                        "endpoint2", Map.of("GET", 30L, "PUT", 10L)
                ),
                logProccesor::conteoPeticionesPorEndpointYMetodo,
                reportController::getConteoPeticionesPorMetodo
        );
    }

    @Test
    void testGetEventosCriticos() {
        List<String> mockEventosCriticos = List.of("Critical Event 1", "Critical Event 2");
        when(logProccesor.filtrarEventosCriticos()).thenReturn(mockEventosCriticos);

        ResponseEntity<List<String>> response = reportController.getEventosCriticos();
        assertEquals(ResponseEntity.ok(mockEventosCriticos), response);
    }

    @Test
    void testGetCantidadEventosCriticos() {
        Map<String, Long> mockCantidadEventosCriticos = Map.of("CriticalEventType1", 5L, "CriticalEventType2", 3L);
        when(logProccesor.generarReporteCantidadEventosCriticos()).thenReturn(mockCantidadEventosCriticos);

        ResponseEntity<Map<String, Long>> response = reportController.getCantidadEventosCriticos();
        assertEquals(ResponseEntity.ok(mockCantidadEventosCriticos), response);
    }

    @Test
    void testGetResumenTotal() {
        Map<String, Object> mockResumenTotal = Map.of("totalErrors", 15, "totalRequests", 100);
        when(logProccesor.generarResumenTotal()).thenReturn(mockResumenTotal);

        ResponseEntity<Map<String, Object>> response = reportController.getResumenTotal();
        assertEquals(ResponseEntity.ok(mockResumenTotal), response);
    }

    private <T> void testWithMockedReturn(Supplier<T> mockSupplier, Supplier<T> mockMethod, Supplier<T> actualMethod) {
        T mockReturn = mockSupplier.get();
        when(mockMethod.get()).thenReturn(mockReturn);
        assertEquals(mockReturn, actualMethod.get());
    }
}
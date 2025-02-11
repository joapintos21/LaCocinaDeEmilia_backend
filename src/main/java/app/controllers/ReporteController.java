package app.controllers;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Base64;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.GET;

import app.services.ReporteService;

public class ReporteController extends AppController {

    private static final Logger logger = LogManager.getLogger(ReporteController.class);

    private void eliminarArchivo(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                logger.info("Archivo eliminado: " + filePath);
            }
        } catch (IOException e) {
            logger.error("Error al eliminar el archivo: " + filePath + " - " + e.getMessage());
        }
    }

    @GET
    public void generarReportePedidos() {
        String fechaInicio = param("fechaInicio");
        String fechaFin = param("fechaFin");
        String formato = param("formato") != null ? param("formato").trim().toLowerCase() : "pdf";
        String tipo = "pedidos";
        String extension = "excel".equals(formato) || "xlsx".equals(formato) ? "xlsx" : "pdf";
        String filePath = "reporte_" + tipo + "." + extension;

        System.out.println("Generando reporte de pedidos en formato " + formato.toUpperCase());

        try {
            ReporteService reporteService = new ReporteService();
            reporteService.generarReportePedidos(fechaInicio, fechaFin, formato);

            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
            String base64EncodedFile = Base64.getEncoder().encodeToString(fileBytes);
            logger.info("Reporte de pedidos generado correctamente.");
            eliminarArchivo(filePath);

            respond("{\"file\": \"" + base64EncodedFile + "\"}")
                    .contentType("application/json")
                    .status(200);
        } catch (Exception e) {
            logger.error("Error al generar el reporte: " + e.getMessage());
            respond("{\"error\": \"Error al generar el reporte: " + e.getMessage() + "\"}")
                    .contentType("application/json")
                    .status(500);
        }
    }

    @GET
    public void generarReporteUsuarios() {
        String formato = param("formato");
        String tipo = "usuarios"; // 🔹 Definimos tipo correctamente
        String extension = formato.equalsIgnoreCase("excel") ? "xlsx" : "pdf";
        String filePath = "reporte_" + tipo + "." + extension;

        System.out.println("Generando reporte de usuarios en formato " + formato.toUpperCase());

        try {
            ReporteService reporteService = new ReporteService();
            reporteService.generarReporteUsuarios(formato);

            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
            String base64EncodedFile = Base64.getEncoder().encodeToString(fileBytes);
            logger.info("Reporte de usuarios generado correctamente.");
            eliminarArchivo(filePath);

            respond("{\"file\": \"" + base64EncodedFile + "\"}")
                    .contentType("application/json")
                    .status(200);
        } catch (Exception e) {
            logger.error("Error al generar el reporte: " + e.getMessage());
            respond("{\"error\": \"Error al generar el reporte: " + e.getMessage() + "\"}")
                    .contentType("application/json")
                    .status(500);
        }
    }

    @GET
    public void generarReporteGastos() {
        String fechaInicio = param("fechaInicio");
        String fechaFin = param("fechaFin");
        String formato = param("formato");
        String tipo = "gastos";
        String extension = formato.equalsIgnoreCase("excel") || formato.equalsIgnoreCase("xlsx") ? "xlsx" : "pdf";
        String filePath = "reporte_" + tipo + "." + extension;

        System.out.println("Generando reporte de gastos en formato " + formato.toUpperCase());

        try {
            ReporteService reporteService = new ReporteService();
            reporteService.generarReporteGastos(fechaInicio, fechaFin, formato);

            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
            String base64EncodedFile = Base64.getEncoder().encodeToString(fileBytes);
            logger.info("Reporte de gastos generado correctamente.");
            eliminarArchivo(filePath);

            respond("{\"file\": \"" + base64EncodedFile + "\"}")
                    .contentType("application/json")
                    .status(200);
        } catch (Exception e) {
            logger.error("Error al generar el reporte: " + e.getMessage());
            respond("{\"error\": \"Error al generar el reporte: " + e.getMessage() + "\"}")
                    .contentType("application/json")
                    .status(500);
        }
    }
}

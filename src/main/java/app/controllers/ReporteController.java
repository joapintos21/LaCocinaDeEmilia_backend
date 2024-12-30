package app.controllers;

import app.services.ReportePDF;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.GET;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class ReporteController extends AppController {

    @GET
    public void generarReportePedidos() {
        System.out.println("Solicitud recibida para generar reporte de pedidos");
        try {
            // Genera el reporte y obtiene la ruta del archivo
            ReportePDF reportePDF = new ReportePDF();
            reportePDF.generarReportePDF();

            // Lee el archivo PDF como bytes
            byte[] pdfBytes = Files.readAllBytes(Paths.get("reporte.pdf"));

            // Convierte los bytes a Base64
            String base64EncodedPdf = Base64.getEncoder().encodeToString(pdfBytes);

            // Envía la respuesta con el PDF codificado
            respond("{\"file\": \"" + base64EncodedPdf + "\"}")
                    .contentType("application/json")
                    .status(200);
        } catch (Exception e) {
            e.printStackTrace();
            // Manejo de errores
            respond("{\"error\": \"Error al generar el reporte: " + e.getMessage() + "\"}")
                    .contentType("application/json")
                    .status(500);
        }
    }
}

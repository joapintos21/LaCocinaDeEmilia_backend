package app.controllers;

import app.services.ReportePDF;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.time.LocalDate;

public class ReporteController extends AppController {

    @GET
    public void generarReportePedidos() {
        String fechaInicio = param("fechaInicio");
        String fechaFin = param("fechaFin");

        System.out.println("Generando reporte de pedidos desde " + fechaInicio + " hasta " + fechaFin);

        try {
            ReportePDF reportePDF = new ReportePDF();
            reportePDF.generarReportePDF(fechaInicio, fechaFin);
            byte[] pdfBytes = Files.readAllBytes(Paths.get("reporte.pdf"));
            String base64EncodedPdf = Base64.getEncoder().encodeToString(pdfBytes);

            respond("{\"file\": \"" + base64EncodedPdf + "\"}")
                    .contentType("application/json")
                    .status(200);
        } catch (Exception e) {
            e.printStackTrace();
            respond("{\"error\": \"Error al generar el reporte: " + e.getMessage() + "\"}")
                    .contentType("application/json")
                    .status(500);
        }
    }

    @GET
    public void generarReporteUsuarios() {
        System.out.println("Solicitud recibida para generar reporte de usuarios");
        try {
            ReportePDF reportePDF = new ReportePDF();
            reportePDF.generarReportePDFUsuarios();

            byte[] pdfBytes = Files.readAllBytes(Paths.get("reporte_usuarios.pdf"));
            String base64EncodedPdf = Base64.getEncoder().encodeToString(pdfBytes);

            respond("{\"file\": \"" + base64EncodedPdf + "\"}")
                    .contentType("application/json")
                    .status(200);
        } catch (Exception e) {
            e.printStackTrace();
            respond("{\"error\": \"Error al generar el reporte: " + e.getMessage() + "\"}")
                    .contentType("application/json")
                    .status(500);
        }
    }

    @GET
    public void generarReporteGastos() {
        String fechaInicio = param("fechaInicio");
        String fechaFin = param("fechaFin");

        System.out.println("Generando reporte de gastos desde " + fechaInicio + " hasta " + fechaFin);

        try {
            ReportePDF reportePDF = new ReportePDF();
            reportePDF.generarReportePDFGastos(fechaInicio, fechaFin);

            byte[] pdfBytes = Files.readAllBytes(Paths.get("reporte_gastos.pdf"));
            String base64EncodedPdf = Base64.getEncoder().encodeToString(pdfBytes);

            respond("{\"file\": \"" + base64EncodedPdf + "\"}")
                    .contentType("application/json")
                    .status(200);
        } catch (Exception e) {
            e.printStackTrace();
            respond("{\"error\": \"Error al generar el reporte: " + e.getMessage() + "\"}")
                    .contentType("application/json")
                    .status(500);
        }
    }
}

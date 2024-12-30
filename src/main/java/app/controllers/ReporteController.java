package app.controllers;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.export.*;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.sql.Connection;
import java.util.Properties;
import java.sql.DriverManager;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.GET;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ReporteController extends AppController {

    @GET
    public void generarReportePedidos() {
        System.out.println("Solicitud recibida para generar reporte de pedidos");
        try {
            // Cargar el archivo compilado JasperReport
            InputStream reportStream = getClass()
                    .getResourceAsStream("/ReportesJasper/ReportePedidos.jasper");
            if (reportStream == null) {
                throw new Exception("No se encontró el reporte Jasper en la ruta especificada.");
            }

            // Conectar a la base de datos
            JasperPrint jasperPrint = JasperFillManager.fillReport(reportStream, new HashMap<>(), obtenerConexion());

            // Exportar el reporte a PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);

            // Codificar el PDF en Base64 para enviarlo como string
            String base64EncodedPdf = java.util.Base64.getEncoder().encodeToString(outputStream.toByteArray());

            // Configurar y enviar la respuesta
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

    private Connection obtenerConexion() throws Exception {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                throw new RuntimeException("No se encontró el archivo database.properties");
            }
            props.load(input);
        }

        String url = props.getProperty("development.url");
        String user = props.getProperty("development.username");
        String password = props.getProperty("development.password");

        return DriverManager.getConnection(url, user, password);
    }
}

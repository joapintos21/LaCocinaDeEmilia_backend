package app.services;

import net.sf.jasperreports.engine.*;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Properties;

public class ReportePDF {
    public void generarReportePDF() throws Exception {
        // Carga el archivo .jasper
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                "src/main/resources/ReportesJasper/ReportePedidos.jasper", new HashMap<>(), obtenerConexion());

        // Exporta a PDF
        JasperExportManager.exportReportToPdfFile(
                jasperPrint, "reporte.pdf");
        System.out.println("Reporte generado: reporte.pdf");
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

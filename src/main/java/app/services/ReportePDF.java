package app.services;

import net.sf.jasperreports.engine.*;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Properties;
import java.sql.Date;

public class ReportePDF {

    public void generarReportePDF(String fechaInicio, String fechaFin) throws Exception {
        // Convertir las fechas de String a java.sql.Date
        Date fechaInicioSQL = Date.valueOf(fechaInicio); // Formato esperado: yyyy-MM-dd
        Date fechaFinSQL = Date.valueOf(fechaFin);
    
        // Crea un mapa de parámetros para JasperReports
        HashMap<String, Object> parametros = new HashMap<>();
        parametros.put("fechaInicio", fechaInicioSQL);
        parametros.put("fechaFin", fechaFinSQL);
    
        // Llena el reporte con los parámetros y la conexión
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                "src/main/resources/ReportesJasper/ReportePedidos.jasper", parametros, obtenerConexion());
    
        // Exporta a PDF
        JasperExportManager.exportReportToPdfFile(
                jasperPrint, "reporte.pdf");
        System.out.println("Reporte generado: reporte.pdf");
    }
    
    public void generarReportePDFUsuarios() throws Exception {
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                "src/main/resources/ReportesJasper/ReporteUsuarios.jasper", new HashMap<>(), obtenerConexion());

        // Exporta el reporte a PDF
        JasperExportManager.exportReportToPdfFile(
                jasperPrint, "reporte_usuarios.pdf");
        System.out.println("Reporte generado: reporte_usuarios.pdf");
    }

    public void generarReportePDFGastos(String fechaInicio, String fechaFin) throws Exception {
        // Convertir las fechas a java.sql.Date
        Date fechaInicioSQL = Date.valueOf(fechaInicio);
        Date fechaFinSQL = Date.valueOf(fechaFin);
    
        // Mapa de parámetros
        HashMap<String, Object> parametros = new HashMap<>();
        parametros.put("fechaInicio", fechaInicioSQL);
        parametros.put("fechaFin", fechaFinSQL);
    
        // Llena el reporte
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                "src/main/resources/ReportesJasper/ReporteGastos.jasper", parametros, obtenerConexion());
    
        JasperExportManager.exportReportToPdfFile(jasperPrint, "reporte_gastos.pdf");
        System.out.println("Reporte de gastos generado: reporte_gastos.pdf");
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

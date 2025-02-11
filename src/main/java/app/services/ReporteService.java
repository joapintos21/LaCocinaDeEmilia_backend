package app.services;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Properties;
import java.sql.Date;

public class ReporteService {

    public void generarReportePedidos(String fechaInicio, String fechaFin, String formato) throws Exception {
        String formatoLimpio = formato != null ? formato.trim().toLowerCase() : "pdf";
        System.out.println("Formato recibido para pedidos: " + formatoLimpio);
    
        Date fechaInicioSQL = Date.valueOf(fechaInicio);
        Date fechaFinSQL = Date.valueOf(fechaFin);
        
        HashMap<String, Object> parametros = new HashMap<>();
        parametros.put("fechaInicio", fechaInicioSQL);
        parametros.put("fechaFin", fechaFinSQL);
        
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                "src/main/resources/ReportesJasper/ReportePedidos.jasper", parametros, obtenerConexion());
        
        if ("excel".equals(formatoLimpio) || "xlsx".equals(formatoLimpio)) {
            exportarReporteAExcel(jasperPrint, "reporte_pedidos.xlsx");
            System.out.println("Reporte de pedidos generado en Excel: reporte_pedidos.xlsx");
        } else {
            JasperExportManager.exportReportToPdfFile(jasperPrint, "reporte_pedidos.pdf");
            System.out.println("Reporte de pedidos generado en PDF: reporte_pedidos.pdf");
        }
    }

    public void generarReporteUsuarios(String formato) throws Exception {
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                "src/main/resources/ReportesJasper/ReporteUsuarios.jasper", new HashMap<>(), obtenerConexion());

        if ("excel".equalsIgnoreCase(formato) || "xlsx".equalsIgnoreCase(formato)) {
            exportarReporteAExcel(jasperPrint, "reporte_usuarios.xlsx");
        } else {
            JasperExportManager.exportReportToPdfFile(jasperPrint, "reporte_usuarios.pdf");
        }
    }

    public void generarReporteGastos(String fechaInicio, String fechaFin, String formato) throws Exception {
        Date fechaInicioSQL = Date.valueOf(fechaInicio);
        Date fechaFinSQL = Date.valueOf(fechaFin);
    
        HashMap<String, Object> parametros = new HashMap<>();
        parametros.put("fechaInicio", fechaInicioSQL);
        parametros.put("fechaFin", fechaFinSQL);
    
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                "src/main/resources/ReportesJasper/ReporteGastos.jasper", parametros, obtenerConexion());
    
        if ("excel".equalsIgnoreCase(formato) || "xlsx".equalsIgnoreCase(formato)) {
            exportarReporteAExcel(jasperPrint, "reporte_gastos.xlsx");
            System.out.println("Reporte de gastos generado en Excel: reporte_gastos.xlsx");
        } else {
            JasperExportManager.exportReportToPdfFile(jasperPrint, "reporte_gastos.pdf");
            System.out.println("Reporte de gastos generado en PDF: reporte_gastos.pdf");
        }
    }

    private void exportarReporteAExcel(JasperPrint jasperPrint, String fileName) throws JRException {
        JRXlsxExporter exporter = new JRXlsxExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(fileName));
    
        SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
        configuration.setOnePagePerSheet(false);          // Coloca todo el contenido en varias hojas si es necesario
        configuration.setDetectCellType(true);            // Detecta el tipo de celda automáticamente (texto, número, etc.)
        configuration.setCollapseRowSpan(false);          // Mantiene las celdas con RowSpan correctamente
        configuration.setRemoveEmptySpaceBetweenRows(true); // Elimina los espacios en blanco entre filas
        configuration.setRemoveEmptySpaceBetweenColumns(true); // Elimina los espacios en blanco entre columnas
        configuration.setWhitePageBackground(false);      // Evita errores de fondo blanco en la página
        configuration.setIgnoreGraphics(true);            // Ignora gráficos para evitar problemas de compatibilidad con Excel
    
        exporter.setConfiguration(configuration);
        exporter.exportReport();
    
        System.out.println("Reporte exportado a Excel: " + fileName);
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

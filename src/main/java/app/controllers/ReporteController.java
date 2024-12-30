import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.export.*;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.sql.Connection;
import java.util.Properties;
import java.sql.DriverManager;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.AppController;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ReporteController extends AppController {

    @GET 
    public ResponseEntity<?> generarReportePedidos() {
        System.out.println("Solicitud recibida para generar reporte de pedidos");
        try {
            // Cargar el archivo compilado JasperReport
            InputStream reportStream = getClass().getResourceAsStream("/ReportesJasper/ReportePedidos");

            // Conectar a la base de datos
            JasperPrint jasperPrint = JasperFillManager.fillReport(reportStream, new HashMap<>(), obtenerConexion());

            // Exportar el reporte a PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);

            // Configurar el nombre y tipo del archivo
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("Reporte_Pedidos.pdf").build());

            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al generar el reporte: " + e.getMessage());
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

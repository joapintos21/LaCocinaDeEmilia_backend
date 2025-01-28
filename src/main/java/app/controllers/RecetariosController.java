package app.controllers;

import java.io.IOException;    

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.POST;
import org.json.JSONObject;

import app.config.FirebaseConfig;
import app.models.ProductosPDF;
import app.services.FirebaseStorageService;

public class RecetariosController extends AppController {

    private static final Logger logger = LogManager.getLogger(RecetariosController.class);

    @POST
    public void subirPDF() {
        try {
            FirebaseConfig.initializeFirebase(); // Asegura que Firebase está inicializado

            JSONObject body = new JSONObject(getRequestString());
            int productoId = body.getInt("producto_id");
            String pdfBase64 = body.optString("pdf", null);

            if (pdfBase64 != null && !pdfBase64.isEmpty()) {
                String pdfUrl = FirebaseStorageService.subirPDF(pdfBase64); // Subimos a Firebase

                // Guardamos en la base de datos
                ProductosPDF prod = new ProductosPDF();
                prod.set("producto_id", productoId);
                prod.set("pdf_url", pdfUrl);
                prod.saveIt();

                JSONObject responseJson = new JSONObject();
                responseJson.put("producto_id", productoId);
                responseJson.put("pdfUrl", pdfUrl);
                logger.info(responseJson.toString());
                respond(responseJson.toString()).contentType("application/json").status(201);
            } else {
                logger.error("No se recibió un archivo válido. ON subirPDF");
                respond("{\"error\": \"No se recibió un archivo válido\"}").contentType("application/json").status(400);
            }
        } catch (IOException e) {
            logger.error("Error al inicializar Firebase: " + e.getMessage());
            respond("{\"error\": \"Error al inicializar Firebase: " + e.getMessage() + "\"}")
                    .contentType("application/json").status(500);
        } catch (Exception e) {
            logger.error(e.getMessage());
            respond("{\"error\": \"" + e.getMessage() + "\"}").contentType("application/json").status(500);
        }
    }
}
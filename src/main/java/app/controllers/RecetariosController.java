package app.controllers;

import app.config.FirebaseConfig;
import app.models.ProductosPDF;
import app.services.FirebaseStorageService;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.POST;
import org.json.JSONObject;

import java.io.IOException;

public class RecetariosController extends AppController {

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
                respond(responseJson.toString()).contentType("application/json").status(201);
            } else {
                respond("{\"error\": \"No se recibió un archivo válido\"}").contentType("application/json").status(400);
            }
        } catch (IOException e) {
            respond("{\"error\": \"Error al inicializar Firebase: " + e.getMessage() + "\"}")
                    .contentType("application/json").status(500);
        } catch (Exception e) {
            respond("{\"error\": \"" + e.getMessage() + "\"}").contentType("application/json").status(500);
        }
    }
}
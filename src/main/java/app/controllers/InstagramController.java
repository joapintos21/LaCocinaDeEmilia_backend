package app.controllers;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.GET;
import org.json.JSONArray;
import org.json.JSONObject;

public class InstagramController extends AppController {
    private static final String INSTAGRAM_USER_ID = "8749086781868712";
    private static final String INSTAGRAM_ACCESS_TOKEN = "IGAB71kP60u6RBZAFAyLWg5ak55TWoyZAlNVX3RCbi1GWVN1eEt1bnlETm9vdVM5MmlLc0RDbjhJYTNjRkVqLVo1QnFXV1c4ZA0ozSG9JWUFIdmRCRmZAaY210emJudGQwaFN4cENxczdlTldobFFuZAFhPek1QbVZA2Vm5MMGtzOU9JNAZDZD\n";
    
    private static final Logger logger = LogManager.getLogger(InstagramController.class);

    @GET
    public void obtenerMedia() {
        try {
            // URL para la API de Instagram
            String apiUrl = "https://graph.instagram.com/" + INSTAGRAM_USER_ID + "/media"
                    + "?fields=id,media_type,media_url,thumbnail_url,permalink,caption,timestamp"
                    + "&limit=5"
                    + "&access_token=" + INSTAGRAM_ACCESS_TOKEN;

            // Configuración de la conexión
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");

            // Leer respuesta
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            reader.close();

            JSONObject responseJson = new JSONObject(responseBuilder.toString());
            JSONArray mediaData = responseJson.getJSONArray("data");

            // Responder con los datos al frontend
            logger.info(mediaData.toString());
            respond(mediaData.toString())
                    .contentType("application/json")
                    .status(200);

        } catch (Exception e) {
            logger.error(e.getMessage());
            respond("{\"error\": \"" + e.getMessage() + "\"}")
                    .contentType("application/json")
                    .status(500);
        }
    }
}
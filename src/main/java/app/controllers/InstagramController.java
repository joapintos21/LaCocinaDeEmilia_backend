package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.GET;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class InstagramController extends AppController {

    // private static final String INSTAGRAM_ACCESS_TOKEN =
    // "IGAB71kP60u6RBZAFBuYWhjU0xFX243bVhkVnVQckctZATlSSHRtZAnF1NzFtdC1ZAdlRhd3lWaGZAyZAFNZAWHJYZA0RSNUV5Q19LOWFWMldodzVhbl9xT3dodnN3dkt1eTZAvMzFIQmNqRWZAiVnBwN0lSYlFBN2FpdjZAiMmM1QkpxOGRMYwZDZD";
    // private static final String INSTAGRAM_USER_ID = "8816356138401496";
    private static final String INSTAGRAM_USER_ID = "9273154079403236";
    private static final String INSTAGRAM_ACCESS_TOKEN = "IGAB71kP60u6RBZAE5jRmp6NVpsdnZABN2lVajBPS1VMaHBFREUzZAFB2WDdnLUJQM0lCdUlRNEswcE9zcVFPaTJrSTJBclpaV2R4SUtCM09kM0l5SW5CUFk1R0J3cW9fYkF5RlJQa3FPVzBqM24zcDlvRGZAYM1VKb1NmbDFiY3Q2OAZDZD";

    @GET
    public void obtenerMedia() {
        try {
            // URL para la API de Instagram
            String apiUrl = "https://graph.instagram.com/" + INSTAGRAM_USER_ID + "/media"
                    + "?fields=id,media_type,media_url,thumbnail_url,permalink,caption,timestamp"
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

            // Convertir respuesta en JSON
            JSONObject responseJson = new JSONObject(responseBuilder.toString());
            JSONArray mediaData = responseJson.getJSONArray("data");

            // Responder con los datos al frontend
            respond(mediaData.toString())
                    .contentType("application/json")
                    .status(200);

        } catch (Exception e) {
            // Manejar errores
            respond("{\"error\": \"" + e.getMessage() + "\"}")
                    .contentType("application/json")
                    .status(500);
        }
    }
}
package app.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.POST;
import org.json.JSONObject;

import app.models.Usuario;

public class LoginController extends AppController {

    private static final Logger logger = LogManager.getLogger(LoginController.class);

    @POST
    public void iniciarSesion() {
        JSONObject body = new JSONObject(getRequestString());
        String email = body.getString("email");
        String password = body.getString("password");
        String responseJson = "";

        Usuario usuario = Usuario.findFirst("email = ?", email);

        if (usuario != null && usuario.getStatus().equals("activo") && usuario.checkPassword(password)) {
            String token = usuario.generateJwt();

            // Construir el JSON de respuesta
            responseJson = "{ \"token\": \"" + token + "\", \"usuario\": " + usuario.formatoJSON() + " }";
            logger.info(responseJson);
            respond(responseJson).contentType("application/json").status(200);
        } else {
            responseJson = "{ \"error\": \"Email o contraseña incorrectos \"}";
            logger.warn("Intento fallido de login. Email o contraseña incorrectos.");
            respond(responseJson).contentType("application/json").status(401);
        }
    }
}

package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.POST;

import app.models.Usuario;

public class LoginController extends AppController {

    @POST
    public void iniciarSesion() {
        String email = param("email");
        String password = param("password");
        String responseJson = "";

        Usuario usuario = Usuario.findFirst("email = ?", email);

        if (usuario != null && usuario.checkPassword(password)) {
            String token = usuario.generateJwt();

            // Construir el JSON de respuesta
            responseJson = "{ \"token\": \"" + token + "\", \"usuario\": " + usuario.formatoJSON() + " }";

            respond(responseJson).contentType("application/json").status(200);
        } else {
            responseJson = "{ \"error\": \"Email o contraseña incorrectos\"}";
            respond(responseJson).contentType("application/json").status(401);
        }
    }
}

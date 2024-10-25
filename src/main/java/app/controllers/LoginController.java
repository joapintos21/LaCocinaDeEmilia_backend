package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.POST;

import app.models.Usuario;

public class LoginController extends AppController {

    @POST
    public void iniciarSesion() {
        String email = param("email");
        String password = param("password");

        Usuario usuario = Usuario.findFirst("email = ?", email);

        if (usuario != null && usuario.checkPassword(password)) {
            String token = usuario.generateJwt();
            respond("{\"token\": \"" + token + "\"}").contentType("application/json").status(200);
        } else {
            respond("Email o contraseña incorrectos").status(401);
        }
    }
}

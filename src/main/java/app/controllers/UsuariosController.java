package app.controllers;

import app.models.Usuario;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.PUT;

public class UsuariosController extends AppController {

    // Crear un nuevo usuario con un rol
    @POST
    public void create(Usuario user) {
        user.setPassword(user.getString("password"));
        if (user.save()) {
            String rolNombre = user.getString("rol"); // Suponiendo que el rol viene con el usuario
            if (rolNombre != null && !rolNombre.isEmpty()) {
                user.assignRol(rolNombre); // Asigna el rol al usuario
            }
            respond(user.toJson(true)).status(200); // Devolver el usuario creado
        } else {
            respond(user.errors().toString()).status(400);
        }
    }

    // Actualizar un usuario y su rol
    @PUT
    public void update(Usuario newUser) {
        int id = Integer.parseInt(param("id"));
        Usuario usuario = Usuario.findById(id);

        if (usuario != null) {
            usuario.fromMap(newUser.toMap()); // Copia los atributos de newUser
            String password = newUser.getString("password");
            if (password != null && !password.isEmpty()) {
                usuario.setPassword(password);
            }

            if (usuario.save()) {
                String rolNombre = newUser.getString("rol"); // Verifica si se envió un rol
                if (rolNombre != null && !rolNombre.isEmpty()) {
                    usuario.assignRol(rolNombre); // Actualiza el rol del usuario
                }
                respond(usuario.toJson(true)).status(200); // Devolver el usuario actualizado
            } else {
                respond(usuario.errors().toString()).status(400);
            }
        } else {
            respond("Usuario no encontrado").status(404);
        }
    }

    // Métodos index, show, delete... sin cambios
}
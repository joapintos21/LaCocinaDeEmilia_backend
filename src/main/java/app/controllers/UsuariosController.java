package app.controllers;

import app.models.Usuario;
import java.util.List;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.DELETE;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.PUT;
import org.json.JSONArray;

public class UsuariosController extends AppController {

    // Crear un nuevo usuario con un rol
    @POST
    public void create(Usuario user) {
        user.setPassword(user.getString("password"));
        if (user.save()) {
            Integer rolId = user.getInteger("rol_id"); // Suponiendo que el rol viene con el usuario
            if (rolId != null && rolId != 0) {
                user.assignRol(rolId); // Asigna el rol al usuario
            }
            respond(user.formatoJSON().toString()).contentType("application/json").status(200);
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
            String password = newUser.getString("password");
            if (password != null && !password.isEmpty()) {
                usuario.setPassword(password);
            }
            Integer rolId = newUser.getInteger("rol_id"); // Suponiendo que el rol viene con el usuario
            if (rolId != null && rolId > 0) {
                usuario.assignRol(rolId); // Asigna el rol al usuario
            }
            usuario.fromMap(newUser.toMap()); // Copia los atributos de newUser
            if (usuario.save()) {
                respond(usuario.formatoJSON().toString()).contentType("application/json").status(200);
            } else {
                respond(usuario.errors().toString()).contentType("application/json").status(400);
            }
        } else {
            respond("Usuario no encontrado").contentType("application/json").status(404);
        }
    }

    // Obtener todos los usuarios
    @GET
    public void index() {
        List<Usuario> usuarios = Usuario.findAll();
        JSONArray jsonArray = new JSONArray();

        // Crear un JSONObject solo con los atributos deseados
        usuarios.forEach(usuario -> {
            jsonArray.put(usuario.formatoJSON());
        });
        String json = jsonArray.toString();
        respond(json).contentType("application/json");
    }

    // Obtener un usuario por ID
    @GET
    public void show() {
        int id = Integer.parseInt(param("id"));
        Usuario usuario = Usuario.findById(id);

        if (usuario != null) {
            respond(usuario.formatoJSON().toString()).contentType("application/json").status(200);
        } else {
            respond("Usuario no encontrado").contentType("application/json").status(404);
        }
    }

    @DELETE
    public void delete() {
        int id = Integer.parseInt(param("id"));
        Usuario usuario = Usuario.findById(id);
        if (usuario != null) {
            usuario.delete(); // Eliminación del usuario (la eliminación en cascada se maneja en la base de
                              // datos)
            respond("Usuario eliminado correctamente").status(200);
        } else {
            respond("Usuario no encontrado").status(404);
        }
    }
}
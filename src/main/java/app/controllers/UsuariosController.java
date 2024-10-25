package app.controllers;

import app.models.Usuario;

import java.util.List;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.PUT;
import org.json.JSONArray;
import org.json.JSONObject;

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
                Integer rolId = newUser.getInteger("rol_id"); // Suponiendo que el rol viene con el usuario
                if (rolId != null && rolId != 0) {
                    usuario.assignRol(rolId); // Asigna el rol al usuario
                }
                respond(usuario.toJson(true)).status(200); // Devolver el usuario actualizado
            } else {
                respond(usuario.errors().toString()).status(400);
            }
        } else {
            respond("Usuario no encontrado").status(404);
        }
    }

    // Obtener todos los usuarios
    @GET
    public void index() {
        List<Usuario> usuarios = Usuario.findAll();
        JSONArray jsonArray = new JSONArray();

        // Crear un JSONObject solo con los atributos deseados

        usuarios.forEach(usuario -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", usuario.get("id"));
            jsonObject.put("nombre", usuario.get("nombre"));
            jsonObject.put("email", usuario.get("email"));
            jsonObject.put("telefono", usuario.get("telefono"));
            jsonArray.put(jsonObject);
        });
        String json = jsonArray.toString();
        respond(json);
    }

    // Obtener un usuario por ID
    @GET
    public void show() {

        int id = Integer.parseInt(param("id"));
        Usuario usuario = Usuario.findById(id);

        if (usuario != null) {
            respond(usuario.toJson(true)).status(200);
        } else {
            respond("Usuario no encontrado").status(404);
        }
    }

    // Métodos index, show, delete... sin cambios
}
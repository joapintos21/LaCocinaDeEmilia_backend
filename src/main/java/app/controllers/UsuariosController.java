package app.controllers;

import app.models.Usuario;
import java.util.List;
import org.javalite.activejdbc.Base;
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
                user.assignRol(rolId, null); // Asigna el rol al usuario
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
            try {
                Base.openTransaction();

                // Captura el rol actual antes de sobrescribir con fromMap
                Integer currentRolId = usuario.getInteger("rol_id");

                // Copia los atributos de newUser en usuario
                usuario.fromMap(newUser.toMap());

                // Si el campo "password" en newUser no es nulo ni vacío, lo actualizamos
                String password = newUser.getString("password");
                if (password != null && !password.isEmpty()) {
                    usuario.setPassword(password);
                }

                // Verificamos si rol_id cambió y actualizamos si es necesario
                Integer newRolId = usuario.getInteger("rol_id");
                if (newRolId != null && !newRolId.equals(currentRolId)) {
                    usuario.assignRol(newRolId, currentRolId); // Asigna el nuevo rol al usuario
                }
                System.out.println(usuario);
                if (usuario.saveIt()) {
                    Base.commitTransaction();
                    respond(usuario.formatoJSON().toString()).contentType("application/json").status(200);
                } else {
                    Base.rollbackTransaction();
                    System.out.println(usuario.errors());
                    respond(usuario.errors().toString()).contentType("application/json").status(400);
                }
            } catch (Exception e) {
                Base.rollbackTransaction();
                System.out.println(e.getMessage());
                respond("Error al actualizar el usuario").contentType("application/json").status(500);
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
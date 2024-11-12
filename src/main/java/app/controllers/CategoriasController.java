package app.controllers;

import app.models.Categoria;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.DELETE;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.PUT;
import org.json.JSONArray;

import java.util.List;

public class CategoriasController extends AppController {

    // Crear una nueva categoría
    @POST
    public void create(Categoria categoria) {
        try {
            if (categoria.save()) {
                respond(categoria.toJson().toString()).contentType("application/json").status(200);
            } else {
                String errorJson = "{\"error\": " + categoria.errors().toJSON() + "}";
                respond(errorJson).contentType("application/json").status(400);
            }
        } catch (Exception e) {
            String errorJson = "{\"error\": \"" + e.getMessage() + "\"}";
            respond(errorJson).contentType("application/json").status(500);
        }
    }

    // Actualizar una categoría
    @PUT
    public void update(Categoria newCategoria) {
        int id = Integer.parseInt(param("id"));
        Categoria categoria = Categoria.findById(id);

        if (categoria != null) {
            try {
                categoria.fromMap(newCategoria.toMap());
                if (categoria.saveIt()) {
                    respond(categoria.toJson().toString()).contentType("application/json").status(200);
                } else {
                    respond(categoria.errors().toJSON()).contentType("application/json").status(400);
                }
            } catch (Exception e) {
                respond("Error al actualizar la categoría").contentType("application/json").status(500);
            }
        } else {
            respond("Categoría no encontrada").contentType("application/json").status(404);
        }
    }

    // Obtener todas las categorías
    @GET
    public void index() {
        List<Categoria> categorias = Categoria.findAll();
        JSONArray jsonArray = new JSONArray();
        categorias.forEach(categoria -> jsonArray.put(categoria.toJson()));
        respond(jsonArray.toString()).contentType("application/json");
    }

    // Obtener una categoría por ID
    @GET
    public void show() {
        int id = Integer.parseInt(param("id"));
        Categoria categoria = Categoria.findById(id);

        if (categoria != null) {
            respond(categoria.toJson().toString()).contentType("application/json").status(200);
        } else {
            respond("Categoría no encontrada").contentType("application/json").status(404);
        }
    }

    // Eliminar una categoría
    @DELETE
    public void delete() {
        int id = Integer.parseInt(param("id"));
        Categoria categoria = Categoria.findById(id);
        if (categoria != null) {
            categoria.delete();
            respond("Categoría eliminada correctamente").status(200);
        } else {
            respond("Categoría no encontrada").status(404);
        }
    }
}

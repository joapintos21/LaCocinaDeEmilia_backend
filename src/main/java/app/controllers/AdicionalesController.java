package app.controllers;

import app.models.Adicional;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.DELETE;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.PUT;
import org.json.JSONArray;

import java.util.List;

public class AdicionalesController extends AppController {

    // Crear un nuevo adicional
    @POST
    public void create(Adicional adicional) {
        try {
            if (adicional.save()) {
                respond(adicional.toJson().toString()).contentType("application/json").status(201);
            } else {
                String errorJson = "{\"error\": " + adicional.errors().toJSON() + "}";
                respond(errorJson).contentType("application/json").status(400);
            }
        } catch (Exception e) {
            String errorJson = "{\"error\": \"" + e.getMessage() + "\"}";
            respond(errorJson).contentType("application/json").status(500);
        }
    }

    // Actualizar un adicional
    @PUT
    public void update(Adicional newAdicional) {
        int id = Integer.parseInt(param("id"));
        Adicional adicional = Adicional.findById(id);

        if (adicional != null) {
            try {
                adicional.fromMap(newAdicional.toMap());
                if (adicional.saveIt()) {
                    respond(adicional.toJson().toString()).contentType("application/json").status(200);
                } else {
                    respond(adicional.errors().toJSON()).contentType("application/json").status(400);
                }
            } catch (Exception e) {
                respond("{\"error\": \"Error al actualizar el adicional\"}")
                        .contentType("application/json").status(500);
            }
        } else {
            respond("{\"error\": \"Adicional no encontrado\"}").contentType("application/json").status(404);
        }
    }

    // Obtener todos los adicionales
    @GET
    public void index() {
        List<Adicional> adicionales = Adicional.findAll();
        JSONArray jsonArray = new JSONArray();
        adicionales.forEach(adicional -> jsonArray.put(adicional.toJson()));
        respond(jsonArray.toString()).contentType("application/json");
    }

    // Obtener un adicional por ID
    @GET
    public void show() {
        int id = Integer.parseInt(param("id"));
        Adicional adicional = Adicional.findById(id);

        if (adicional != null) {
            respond(adicional.toJson().toString()).contentType("application/json").status(200);
        } else {
            respond("{\"error\": \"Adicional no encontrado\"}").contentType("application/json").status(404);
        }
    }

    // Eliminar un adicional
    @DELETE
    public void delete() {
        int id = Integer.parseInt(param("id"));
        Adicional adicional = Adicional.findById(id);
        if (adicional != null) {
            adicional.delete();
            respond("{\"message\": \"Adicional eliminado correctamente\"}")
                    .contentType("application/json").status(200);
        } else {
            respond("{\"error\": \"Adicional no encontrado\"}").contentType("application/json").status(404);
        }
    }
}
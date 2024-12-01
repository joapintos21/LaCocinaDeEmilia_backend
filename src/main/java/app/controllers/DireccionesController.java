package app.controllers;

import app.models.Direccion;
import app.models.Usuario;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.DELETE;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.PUT;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class DireccionesController extends AppController {

    // Crear una nueva dirección
    @POST
    public void create(Direccion direccion) {
        try {
            if (direccion.saveIt()) {
                respond(direccion.toJson().toString()).contentType("application/json").status(201);
            } else {
                String errorJson = "{\"error\": " + direccion.errors().toJSON() + "}";
                respond(errorJson).contentType("application/json").status(400);
            }
        } catch (Exception e) {
            String errorJson = "{\"error\": \"" + e.getMessage() + "\"}";
            respond(errorJson).contentType("application/json").status(500);
        }
    }

    // Actualizar una dirección existente
    @PUT
    public void update(Direccion newDireccion) {
        int id = Integer.parseInt(param("id"));
        Direccion direccion = Direccion.findById(id);

        if (direccion != null) {
            try {
                direccion.fromMap(newDireccion.toMap());
                if (direccion.saveIt()) {
                    respond(direccion.toJson().toString()).contentType("application/json").status(200);
                } else {
                    respond(direccion.errors().toJSON()).contentType("application/json").status(400);
                }
            } catch (Exception e) {
                respond("{\"error\": \"Error al actualizar la dirección\"}")
                        .contentType("application/json").status(500);
            }
        } else {
            respond("{\"error\": \"Dirección no encontrada\"}")
                    .contentType("application/json").status(404);
        }
    }

    // Listar todas las direcciones de un cliente
    @GET
    public void index() {
        int clienteId = Integer.parseInt(param("cliente_id"));
        List<Direccion> direcciones = Direccion.where("cliente_id = ?", clienteId);

        JSONArray jsonArray = new JSONArray();
        direcciones.forEach(direccion -> jsonArray.put(direccion.toJson()));
        respond(jsonArray.toString()).contentType("application/json").status(200);
    }

    // Obtener una dirección específica por ID
    @GET
    public void show() {
        int id = Integer.parseInt(param("id"));
        Direccion direccion = Direccion.findById(id);

        if (direccion != null) {
            respond(direccion.toJson().toString()).contentType("application/json").status(200);
        } else {
            respond("{\"error\": \"Dirección no encontrada\"}")
                    .contentType("application/json").status(404);
        }
    }

    // Eliminar una dirección
    @DELETE
    public void delete() {
        int id = Integer.parseInt(param("id"));
        Direccion direccion = Direccion.findById(id);

        if (direccion != null) {
            direccion.delete();
            respond("{\"message\": \"Dirección eliminada correctamente\"}")
                    .contentType("application/json").status(200);
        } else {
            respond("{\"error\": \"Dirección no encontrada\"}")
                    .contentType("application/json").status(404);
        }
    }

    // Establecer una dirección como predeterminada
    @PUT
    public void setDefault() {
        int id = Integer.parseInt(param("id"));
        Direccion direccion = Direccion.findById(id);

        if (direccion != null) {
            try {
                // Desmarcar otras direcciones del cliente como predeterminadas
                Direccion.update("predeterminado = false", "cliente_id = ?", direccion.get("cliente_id"));

                // Establecer esta dirección como predeterminada
                direccion.set("predeterminado", true);
                direccion.saveIt();

                respond("{\"message\": \"Dirección establecida como predeterminada\"}")
                        .contentType("application/json").status(200);
            } catch (Exception e) {
                respond("{\"error\": \"Error al establecer la dirección como predeterminada\"}")
                        .contentType("application/json").status(500);
            }
        } else {
            respond("{\"error\": \"Dirección no encontrada\"}")
                    .contentType("application/json").status(404);
        }
    }
}
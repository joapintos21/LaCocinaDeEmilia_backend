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

    @POST
    public void create(Direccion direccion) {
        try {
            // Validar si el cliente existe
            if (Usuario.findFirst("id = ?", direccion.get("cliente_id")) == null) {
                respond("{\"error\": \"El cliente no existe\"}").contentType("application/json").status(400);
                return;
            }

            if (direccion.saveIt()) {
                respond(direccion.toJson().toString()).contentType("application/json").status(201);
            } else {
                respond(direccion.errors().toJSON().toString()).contentType("application/json").status(400);
            }
        } catch (Exception e) {
            respond("{\"error\": \"Error inesperado: " + e.getMessage() + "\"}")
                    .contentType("application/json").status(500);
        }
    }

    @PUT
    public void update(Direccion newDireccion) {
        String idParam = param("id");
        if (idParam == null || idParam.isEmpty()) {
            respond("{\"error\": \"El ID es requerido\"}").contentType("application/json").status(400);
            return;
        }
        try {
            int id = Integer.parseInt(idParam);
            Direccion direccion = Direccion.findById(id);
            if (direccion == null) {
                respond("{\"error\": \"Dirección no encontrada\"}").contentType("application/json").status(404);
                return;
            }
            direccion.fromMap(newDireccion.toMap());
            if (direccion.saveIt()) {
                respond(direccion.toJson().toString()).contentType("application/json").status(200);
            } else {
                respond(direccion.errors().toJSON().toString()).contentType("application/json").status(400);
            }
        } catch (Exception e) {
            respond("{\"error\": \"Error inesperado: " + e.getMessage() + "\"}").contentType("application/json").status(500);
        }
    }

    @GET
    public void index() {
        String clienteIdParam = param("cliente_id");
        if (clienteIdParam == null || clienteIdParam.isEmpty()) {
            respond("{\"error\": \"cliente_id es requerido\"}").contentType("application/json").status(400);
            return;
        }
        try {
            int clienteId = Integer.parseInt(clienteIdParam);
            List<Direccion> direcciones = Direccion.where("cliente_id = ?", clienteId);
            JSONArray jsonArray = new JSONArray();
            direcciones.forEach(direccion -> jsonArray.put(direccion.toJson()));
            respond(jsonArray.toString()).contentType("application/json").status(200);
        } catch (Exception e) {
            respond("{\"error\": \"Error inesperado: " + e.getMessage() + "\"}").contentType("application/json").status(500);
        }
    }

    @DELETE
    public void delete() {
        String idParam = param("id");
        if (idParam == null || idParam.isEmpty()) {
            respond("{\"error\": \"El ID es requerido\"}").contentType("application/json").status(400);
            return;
        }
        try {
            int id = Integer.parseInt(idParam);
            Direccion direccion = Direccion.findById(id);
            if (direccion == null) {
                respond("{\"error\": \"Dirección no encontrada\"}").contentType("application/json").status(404);
                return;
            }
            direccion.delete();
            respond("{\"message\": \"Dirección eliminada correctamente\"}").contentType("application/json").status(200);
        } catch (Exception e) {
            respond("{\"error\": \"Error inesperado: " + e.getMessage() + "\"}").contentType("application/json").status(500);
        }
    }

    @PUT
    public void setDefault() {
        String idParam = param("id");
        if (idParam == null || idParam.isEmpty()) {
            respond("{\"error\": \"El ID es requerido\"}").contentType("application/json").status(400);
            return;
        }
        try {
            int id = Integer.parseInt(idParam);
            Direccion direccion = Direccion.findById(id);
            if (direccion == null) {
                respond("{\"error\": \"Dirección no encontrada\"}").contentType("application/json").status(404);
                return;
            }
            // Desmarcar otras direcciones como predeterminadas
            Direccion.update("predeterminado = false", "cliente_id = ?", direccion.get("cliente_id"));
            direccion.set("predeterminado", true);
            direccion.saveIt();
            respond(direccion.toJson().put("message", "Dirección establecida como predeterminada").toString())
                    .contentType("application/json").status(200);
        } catch (Exception e) {
            respond("{\"error\": \"Error inesperado: " + e.getMessage() + "\"}").contentType("application/json").status(500);
        }
    }
}

package app.controllers;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.DELETE;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.PUT;
import org.json.JSONArray;

import app.models.Cliente;
import app.models.Direccion;

public class DireccionesController extends AppController {

    private static final Logger logger = LogManager.getLogger(DireccionesController.class);

    @POST
    public void create(Direccion direccion) {
        try {
            // Validar si el cliente existe
            int clienteId = direccion.getInteger("cliente_id");
            Cliente c = Cliente.findFirst("usuario_id = ?", clienteId);
            if (c == null) {
                logger.error("El cliente " + clienteId + " no existe. ON CREATE");
                respond("{\"error\": \"El cliente no existe\"}").contentType("application/json").status(400);
                return;
            }
            direccion.set("cliente_id", c.getId());

            // Verificar si esta dirección es predeterminada
            Boolean esPredeterminada = direccion.getBoolean("predeterminado");
            if (esPredeterminada != null && esPredeterminada) {
                // Buscar y desmarcar la dirección predeterminada existente, si la hay
                Direccion predeterminadaActual = Direccion.findFirst("cliente_id = ? AND predeterminado = true",
                        c.getId());
                if (predeterminadaActual != null) {
                    predeterminadaActual.set("predeterminado", false).saveIt();
                }
            }

            if (direccion.saveIt()) {
                logger.info(direccion.toJson().toString());
                respond(direccion.toJson().toString()).contentType("application/json").status(201);
            } else {
                logger.error(direccion.errors().toString());
                respond(direccion.errors().toJSON()).contentType("application/json").status(400);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            respond("{\"error\": \"Error inesperado: " + e.getMessage() + "\"}")
                    .contentType("application/json").status(500);
        }
    }

    @PUT
    public void update(Direccion newDireccion) {
        String idParam = param("id");
        if (idParam == null || idParam.isEmpty()) {
            logger.error("El ID de direccion es requerido. ON UPDATE");
            respond("{\"error\": \"El ID es requerido\"}").contentType("application/json").status(400);
            return;
        }
        try {
            int id = Integer.parseInt(idParam);
            Direccion direccion = Direccion.findById(id);
            if (direccion == null) {
                logger.error("Dirección no encontrada. ON UPDATE");
                respond("{\"error\": \"Dirección no encontrada\"}").contentType("application/json").status(404);
                return;
            }
            direccion.fromMap(newDireccion.toMap());

            if (direccion.saveIt()) {
                logger.error(direccion.toString());
                respond(direccion.toJson().toString()).contentType("application/json").status(200);
            } else {
                logger.error(direccion.errors().toString());
                respond(direccion.errors().toJSON()).contentType("application/json").status(400);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            respond("{\"error\": \"Error inesperado: " + e.getMessage() + "\"}").contentType("application/json")
                    .status(500);
        }
    }

    @GET
    public void index() {
        String clienteIdParam = param("cliente_id");
        if (clienteIdParam == null || clienteIdParam.isEmpty()) {
            logger.error("El cliente_id es requerido para la direccion. ON INDEX");
            respond("{\"error\": \"cliente_id es requerido\"}").contentType("application/json").status(400);
            return;
        }
        try {
            int clienteId = Integer.parseInt(clienteIdParam);
            Cliente c = Cliente.findFirst("usuario_id = ?", clienteId);
            List<Direccion> direcciones = Direccion.where("cliente_id = ?", c.getId());
            JSONArray jsonArray = new JSONArray();
            direcciones.forEach(direccion -> jsonArray.put(direccion.toJson()));
            logger.info(jsonArray.toString());
            respond(jsonArray.toString()).contentType("application/json").status(200);
        } catch (Exception e) {
            logger.error(e.getMessage());
            respond("{\"error\": \"Error inesperado: " + e.getMessage() + "\"}").contentType("application/json")
                    .status(500);
        }
    }

    @DELETE
    public void delete() {
        int idParam = Integer.parseInt(param("id"));
        try {
            Direccion direccion = Direccion.findById(idParam);
            if (direccion == null) {
                logger.error("Dirección no encontrada. ON DELETE");
                respond("{\"error\": \"Dirección no encontrada\"}").contentType("application/json").status(404);
                return;
            }

            direccion.delete();
            respond("{\"message\": \"Dirección eliminada correctamente\"}").contentType("application/json").status(200);
        } catch (Exception e) {
            logger.error(e.getMessage());
            respond("{\"error\": \"Error inesperado: " + e.getMessage() + "\"}").contentType("application/json").status(500);
        }
    }

    @PUT
    public void setDefault() {
        String idParam = param("id");
        if (idParam == null || idParam.isEmpty()) {
            logger.error("El ID de direccion es requerido. ON setDefault");
            respond("{\"error\": \"El ID es requerido\"}").contentType("application/json").status(400);
            return;
        }
        try {
            int id = Integer.parseInt(idParam);
            Direccion direccion = Direccion.findById(id);
            if (direccion == null) {
                logger.error("Dirección no encontrada. ON setDefault");
                respond("{\"error\": \"Dirección no encontrada\"}").contentType("application/json").status(404);
                return;
            }
            // Desmarcar otras direcciones como predeterminadas
            Direccion.update("predeterminado = false", "cliente_id = ?", direccion.get("cliente_id"));
            direccion.set("predeterminado", true);
            direccion.saveIt();
            logger.info("Direccion guardada: " + direccion.toString());
            respond(direccion.toJson().put("message", "Dirección establecida como predeterminada").toString())
                    .contentType("application/json").status(200);
        } catch (Exception e) {
            logger.error(e.getMessage());
            respond("{\"error\": \"Error inesperado: " + e.getMessage() + "\"}").contentType("application/json")
                    .status(500);
        }
    }

    @GET
    public void calcularCostoEnvio() {
        try {
            int direccionId = Integer.parseInt(param("direccion_id"));

            // Buscar la dirección
            Direccion direccion = Direccion.findById(direccionId);
            if (direccion == null) {
                logger.error("Direccion no encontrada. ON calcularCostoEnvio");
                respond("{\"error\": \"Dirección no encontrada\"}")
                        .contentType("application/json")
                        .status(404);
                return;
            }

            // Calcular el costo del envío
            double costoEnvio = direccion.calcularCostoEnvio();

            logger.info("costoEnvio: " + costoEnvio);
            respond("{\"costoEnvio\":" + costoEnvio + "}")
                    .contentType("application/json")
                    .status(200);
        } catch (Exception e) {
            logger.error(e.getMessage());
            respond("{\"error\": \"Error inesperado: " + e.getMessage() + "\"}")
                    .contentType("application/json")
                    .status(500);
        }
    }
}

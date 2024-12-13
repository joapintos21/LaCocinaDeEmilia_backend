package app.controllers;

import app.models.Cliente;
import app.models.Direccion;
import app.models.Envio;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.DELETE;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.PUT;
import org.json.JSONArray;

import java.util.List;

public class DireccionesController extends AppController {

    @POST
    public void create(Direccion direccion) {
        try {
            // Validar si el cliente existe
            int clienteId = direccion.getInteger("cliente_id");
            Cliente c = Cliente.findFirst("usuario_id = ?", clienteId);
            if (c == null) {
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
            respond("{\"error\": \"Error inesperado: " + e.getMessage() + "\"}").contentType("application/json")
                    .status(500);
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
            Cliente c = Cliente.findFirst("usuario_id = ?", clienteId);
            List<Direccion> direcciones = Direccion.where("cliente_id = ?", c.getId());
            JSONArray jsonArray = new JSONArray();
            direcciones.forEach(direccion -> jsonArray.put(direccion.toJson()));
            respond(jsonArray.toString()).contentType("application/json").status(200);
        } catch (Exception e) {
            respond("{\"error\": \"Error inesperado: " + e.getMessage() + "\"}").contentType("application/json")
                    .status(500);
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
            respond("{\"error\": \"Error inesperado: " + e.getMessage() + "\"}").contentType("application/json")
                    .status(500);
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
            respond("{\"error\": \"Error inesperado: " + e.getMessage() + "\"}").contentType("application/json")
                    .status(500);
        }
    }

    @GET
    public void calcularCostoEnvio() {
        try {
            int direccionId = Integer.parseInt(param("direccion_id"));
            int pedidoId = Integer.parseInt(param("pedido_id"));

            // Buscar la dirección
            Direccion direccion = Direccion.findById(direccionId);
            if (direccion == null) {
                respond("{\"error\": \"Dirección no encontrada\"}")
                        .contentType("application/json")
                        .status(404);
                return;
            }

            // Calcular el costo del envío
            double costoEnvio = direccion.calcularCostoEnvio();

            // Verificar si ya existe un envío asociado al pedido
            Envio envioExistente = Envio.findFirst("pedido_id = ?", pedidoId);

            if (envioExistente == null) {
                // Crear un nuevo envío
                Envio nuevoEnvio = new Envio();
                nuevoEnvio.set("pedido_id", pedidoId);
                nuevoEnvio.set("direccion_id", direccionId);
                nuevoEnvio.set("costo", costoEnvio);
                nuevoEnvio.set("entregado", false); // Nuevo envío no entregado por defecto

                if (!nuevoEnvio.saveIt()) {
                    respond("{\"error\": \"Error al guardar el envío: " + nuevoEnvio.errors().toJSON().toString()
                            + "\"}")
                            .contentType("application/json")
                            .status(500);
                    return;
                }
            } else {
                // Actualizar el envío existente con la nueva información
                envioExistente.set("direccion_id", direccionId);
                envioExistente.set("costo", costoEnvio);
                envioExistente.saveIt();
            }

            respond("{\"costoEnvio\":" + costoEnvio + "}")
                    .contentType("application/json")
                    .status(200);
        } catch (Exception e) {
            respond("{\"error\": \"Error inesperado: " + e.getMessage() + "\"}")
                    .contentType("application/json")
                    .status(500);
        }
    }
}

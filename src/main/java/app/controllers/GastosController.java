package app.controllers;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.DELETE;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.PUT;
import org.json.JSONArray;
import org.json.JSONObject;

import app.models.Gasto;

public class GastosController extends AppController {

    private static final Logger logger = LogManager.getLogger(GastosController.class);

    @PUT
    public void update() {
        try {
            int id = Integer.parseInt(param("id"));
            JSONObject body = new JSONObject(getRequestString());

            Gasto gasto = Gasto.findById(id);
            if (gasto == null) {
                logger.error("Gasto no encontrado. ON UPDATE");
                respond("{\"error\": \"Gasto no encontrado.\"}")
                        .contentType("application/json")
                        .status(404);
                return;
            }

            if (body.has("descripcion")) {
                String descripcion = body.getString("descripcion").trim();
                if (descripcion.isEmpty()) {
                    logger.error("La descripción no puede estar vacía. ON UPDATE");
                    respond("{\"error\": \"La descripción no puede estar vacía.\"}")
                            .contentType("application/json")
                            .status(400);
                    return;
                }
                gasto.set("descripcion", descripcion);
            }

            if (body.has("monto")) {
                double monto = body.getDouble("monto");
                if (monto <= 0) {
                    logger.error("El monto del gasto debe ser mayor que 0. ON UPDATE");
                    respond("{\"error\": \"El monto debe ser mayor que 0.\"}")
                            .contentType("application/json")
                            .status(400);
                    return;
                }
                gasto.set("monto", monto);
            }

            if (body.has("fecha")) {
                try {
                    String fechaStr = body.getString("fecha");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date fecha = new Date(dateFormat.parse(fechaStr).getTime());
                    gasto.set("fecha", fecha);
                } catch (Exception e) {
                    logger.error("La fecha del gasto no es válida. Debe estar en formato yyyy-MM-dd. ON UPDATE");
                    respond("{\"error\": \"La fecha no es válida. Debe estar en formato yyyy-MM-dd.\"}")
                            .contentType("application/json")
                            .status(400);
                    return;
                }
            }

            if (gasto.save()) {
                logger.info("Gasto actualizado correctamente: " + gasto.toString());
                respond(gasto.toJson().toString())
                        .contentType("application/json")
                        .status(200);
            } else {
                logger.error(gasto.errors().toString());
                respond("{\"error\": " + gasto.errors().toJSON() + "}")
                        .contentType("application/json")
                        .status(400);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            respond("{\"error\": \"Error al actualizar el gasto: " + e.getMessage() + "\"}")
                    .contentType("application/json")
                    .status(500);
        }
    }

    @DELETE
    public void delete() {
        try {
            int id = Integer.parseInt(param("id"));

            Gasto gasto = Gasto.findById(id);
            if (gasto == null) {
                logger.error("Gasto no encontrado. ON DELETE");
                respond("{\"error\": \"Gasto no encontrado.\"}")
                        .contentType("application/json")
                        .status(404);
                return;
            }

            logger.info("Gasto eliminado exitosamente: " + gasto.toString());
            gasto.delete();
            respond("{\"message\": \"Gasto eliminado exitosamente.\"}")
                    .contentType("application/json")
                    .status(200);
        } catch (Exception e) {
            logger.error(e.getMessage());
            respond("{\"error\": \"Error al eliminar el gasto: " + e.getMessage() + "\"}")
                    .contentType("application/json")
                    .status(500);
        }
    }

    @POST
    public void registrarGasto() {
        try {
            JSONObject body = new JSONObject(getRequestString());

            // Validar campos requeridos
            if (!body.has("descripcion") || !body.has("monto") || !body.has("fecha")) {
                logger.error("Los campos 'descripcion', 'monto' y 'fecha' de un Gasto son obligatorios. ON registrarGasto");
                respond("{\"error\": \"Los campos 'descripcion', 'monto' y 'fecha' son obligatorios.\"}")
                        .contentType("application/json")
                        .status(400);
                return;
            }

            String descripcion = body.getString("descripcion");
            double monto = body.getDouble("monto");
            String fechaStr = body.getString("fecha");

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date fecha = new Date(dateFormat.parse(fechaStr).getTime());

            Gasto gasto = new Gasto();
            gasto.set("descripcion", descripcion);
            gasto.set("monto", monto);
            gasto.set("fecha", fecha);
            gasto.set("fecha_carga", new java.sql.Timestamp(System.currentTimeMillis()));

            if (gasto.save()) {
                logger.info("Gasto registrado: " + gasto.toString());
                respond(gasto.toJson().toString()).contentType("application/json").status(201);
            } else {
                logger.error(gasto.errors().toString());
                respond("{\"error\": " + gasto.errors().toJSON() + "}")
                        .contentType("application/json")
                        .status(400);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            respond("{\"error\": \"Error interno del servidor: " + e.getMessage() + "\"}")
                    .contentType("application/json")
                    .status(500);
        }
    }

    @GET
    public void obtenerGastos() {
        try {
            String fechaInicio = param("fechaInicio");
            String fechaFin = param("fechaFin");

            List<Gasto> gastos = Gasto.where("fecha BETWEEN ? AND ?", fechaInicio, fechaFin).orderBy("fecha DESC");

            JSONArray gastosJsonArray = new JSONArray();
            for (Gasto gasto : gastos) {
                gastosJsonArray.put(gasto.toJson());
            }
            logger.info(gastosJsonArray.toString());
            respond(gastosJsonArray.toString()).contentType("application/json").status(200);
        } catch (Exception e) {
            logger.error(e.getMessage());
            respond("Error al obtener los gastos: " + e.getMessage()).status(500);
        }
    }

    @GET
    public void obtenerUltimosGastos() {
        try {
            List<Gasto> gastos = Gasto.findAll().orderBy("fecha_carga DESC").limit(10);

            JSONArray gastosJsonArray = new JSONArray();
            for (Gasto gasto : gastos) {
                gastosJsonArray.put(gasto.toJson());
            }
            logger.info(gastosJsonArray.toString());
            respond(gastosJsonArray.toString()).contentType("application/json").status(200);
        } catch (Exception e) {
            logger.info(e.getMessage());
            respond("{\"error\": \"Error al obtener los últimos gastos: " + e.getMessage() + "\"}")
                    .contentType("application/json")
                    .status(500);
        }
    }
}

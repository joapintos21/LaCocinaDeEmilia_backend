package app.controllers;

import app.models.Gasto;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.PUT;
import org.javalite.activeweb.annotations.DELETE;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.GET;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class GastosController extends AppController {

    @PUT
    public void update() {
        try {
            int id = Integer.parseInt(param("id"));
            JSONObject body = new JSONObject(getRequestString());

            Gasto gasto = Gasto.findById(id);
            if (gasto == null) {
                respond("{\"error\": \"Gasto no encontrado.\"}")
                        .contentType("application/json")
                        .status(404);
                return;
            }

            if (body.has("descripcion")) {
                String descripcion = body.getString("descripcion").trim();
                if (descripcion.isEmpty()) {
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
                    respond("{\"error\": \"La fecha no es válida. Debe estar en formato yyyy-MM-dd.\"}")
                            .contentType("application/json")
                            .status(400);
                    return;
                }
            }

            if (gasto.save()) {
                respond(gasto.toJson().toString())
                        .contentType("application/json")
                        .status(200);
            } else {
                respond("{\"error\": " + gasto.errors().toJSON() + "}")
                        .contentType("application/json")
                        .status(400);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                respond("{\"error\": \"Gasto no encontrado.\"}")
                        .contentType("application/json")
                        .status(404);
                return;
            }

            gasto.delete();
            respond("{\"message\": \"Gasto eliminado exitosamente.\"}")
                    .contentType("application/json")
                    .status(200);
        } catch (Exception e) {
            e.printStackTrace();
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
                respond(gasto.toJson().toString()).contentType("application/json").status(201);
            } else {
                respond("{\"error\": " + gasto.errors().toJSON() + "}")
                        .contentType("application/json")
                        .status(400);
            }
        } catch (Exception e) {
            e.printStackTrace();
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

            respond(gastosJsonArray.toString()).contentType("application/json").status(200);
        } catch (Exception e) {
            e.printStackTrace();
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

            respond(gastosJsonArray.toString()).contentType("application/json").status(200);
        } catch (Exception e) {
            e.printStackTrace();
            respond("{\"error\": \"Error al obtener los últimos gastos: " + e.getMessage() + "\"}")
                    .contentType("application/json")
                    .status(500);
        }
    }
}

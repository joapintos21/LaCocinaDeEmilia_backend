package app.controllers;

import java.sql.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.DELETE;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.PUT;
import org.json.JSONArray;

import app.models.Descuento;
import app.models.TipoDescuento;

public class DescuentosController extends AppController {
    
    private static final Logger logger = LogManager.getLogger(DescuentosController.class);
    // Crear un nuevo descuento
    @POST
    public void create(Descuento descuento) {
        try {
            Descuento desc = new Descuento();
            // Obtener el nombre del tipo de descuento del parámetro
            Integer tipoNombre = descuento.getInteger("tipo_id");
            TipoDescuento tipoDescuento = TipoDescuento.findFirst("id = ?", tipoNombre);

            if (tipoDescuento == null) {
                logger.error("Tipo de descuento inválido. ON CREATE");
                respond("{\"error\": \"Tipo de descuento inválido\"}").contentType("application/json").status(400);
                return;
            }
            desc.set("tipo_id", tipoDescuento.getId());
            desc.set("valor", descuento.get("valor"));
            // Convertir las fechas a java.sql.Date
            String fechaInicio = descuento.getString("fechainicio");
            String fechaFin = descuento.getString("fechafin");

            desc.set("fechainicio", Date.valueOf(fechaInicio)); // Convertir a java.sql.Date

            if (fechaFin != null && !fechaFin.isEmpty()) {
                desc.set("fechafin", Date.valueOf(fechaFin)); // Convertir a java.sql.Date
            } else {
                desc.set("fechafin", null);
            }
            desc.set("descripcion", descuento.get("descripcion"));

            if (desc.save()) {
                logger.info(desc.toJson().toString());
                respond(desc.toJson().toString()).contentType("application/json").status(200);
            } else {
                logger.error(desc.errors().toString());
                respond(desc.errors().toJSON()).contentType("application/json").status(400);
            }
        } catch (IllegalArgumentException e) {
            logger.error("Tipo de descuento inválido. Solo se aceptan 'PORCENTAJE' o 'IMPORTE'. ON CREATE");
            respond("{\"error\": \"Tipo de descuento inválido. Solo se aceptan 'PORCENTAJE' o 'IMPORTE'.\"}")
                    .contentType("application/json").status(400);
        } catch (Exception e) {
            logger.error(e.getMessage());
            respond("{\"error\": \"" + e.getMessage() + "\"}").contentType("application/json").status(500);
        }
    }

    // Obtener todos los descuentos
    @GET
    public void index() {
        List<Descuento> descuentos = Descuento.findAll();
        JSONArray jsonArray = new JSONArray();
        descuentos.forEach(descuento -> jsonArray.put(descuento.toJson()));
        logger.info(jsonArray.toString());
        respond(jsonArray.toString()).contentType("application/json");
    }

    // Obtener tipos de descuentos
    @GET
    public void tiposDescuntos() {
        List<TipoDescuento> tipoDescuentos = TipoDescuento.findAll();
        JSONArray jsonArray = new JSONArray();
        tipoDescuentos.forEach(tipoDescuento -> jsonArray.put(tipoDescuento.toJson()));
        logger.info(jsonArray.toString());
        respond(jsonArray.toString()).contentType("application/json");
    }

    // Obtener un descuento por ID
    @GET
    public void show() {
        int id = Integer.parseInt(param("id"));
        Descuento descuento = Descuento.findById(id);

        if (descuento != null) {
            logger.info(descuento.toJson().toString());
            respond(descuento.toJson().toString()).contentType("application/json").status(200);
        } else {
            logger.error("Descuento no encontrado. ON SHOW");
            respond("{\"error\": \"Descuento no encontrado\"}").contentType("application/json").status(404);
        }
    }

    // Actualizar un descuento
    @PUT
    public void update(Descuento newDescuento) {
        int id = Integer.parseInt(param("id"));
        Descuento descuento = Descuento.findById(id);

        if (descuento != null) {
            try {
                // Obtener el tipo de descuento
                Integer tipoId = newDescuento.getInteger("tipo_id");
                TipoDescuento tipoDescuento = TipoDescuento.findFirst("id = ?", tipoId);

                if (tipoDescuento == null) {
                    logger.error("Tipo de descuento inválido. ON UPDATE: " + descuento.toString());
                    respond("{\"error\": \"Tipo de descuento inválido\"}").contentType("application/json").status(400);
                    return;
                }

                // Actualizar los campos
                descuento.set("tipo_id", tipoDescuento.getId());
                descuento.set("valor", newDescuento.get("valor"));

                // Convertir las fechas a java.sql.Date
                String fechaInicio = newDescuento.getString("fechainicio");
                String fechaFin = newDescuento.getString("fechafin");

                descuento.set("fechainicio", Date.valueOf(fechaInicio)); // Convertir a java.sql.Date

                if (fechaFin != null && !fechaFin.isEmpty()) {
                    descuento.set("fechafin", Date.valueOf(fechaFin)); // Convertir a java.sql.Date
                } else {
                    descuento.set("fechafin", null);
                }

                descuento.set("descripcion", newDescuento.get("descripcion"));

                // Guardar el descuento actualizado
                if (descuento.saveIt()) {
                    logger.info(descuento.toJson().toString());
                    respond(descuento.toJson().toString()).contentType("application/json").status(200);
                } else {
                    logger.error(descuento.errors().toString());
                    respond(descuento.errors().toJSON()).contentType("application/json").status(400);
                }
            } catch (IllegalArgumentException e) {
                logger.error("Fechas inválidas o tipo de descuento no válido. ON UPDATE");
                respond("{\"error\": \"Fechas inválidas o tipo de descuento no válido.\"}")
                        .contentType("application/json").status(400);
            } catch (Exception e) {
                logger.error(e.getMessage());
                String errorJson = "{\"error\": \"" + e.getMessage() + "\"}";
                respond(errorJson).contentType("application/json").status(500);
            }
        } else {
            respond("{\"error\": \"Descuento no encontrado\"}").contentType("application/json").status(404);
        }
    }

    // Eliminar un descuento
    @DELETE
    public void delete() {
        int id = Integer.parseInt(param("id"));
        Descuento descuento = Descuento.findById(id);

        if (descuento != null) {
            logger.info("Descuento eliminado correctamente: " + descuento.toString());
            descuento.delete();
            respond("{\"message\": \"Descuento eliminado correctamente\"}").status(200);
        } else {
            logger.error("Descuento no encontrado. ON DELETE");
            respond("{\"error\": \"Descuento no encontrado\"}").contentType("application/json").status(404);
        }
    }
}

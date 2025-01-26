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

import app.models.Adicional;

public class AdicionalesController extends AppController {
    private static final Logger logger = LogManager.getLogger(AdicionalesController.class);
    // Crear un nuevo adicional
    @POST
    public void create(Adicional adicional) {
        try {
            if (adicional.save()) {
                logger.info(adicional.toJson().toString());
                respond(adicional.toJson().toString()).contentType("application/json").status(201);
            } else {
                String errorJson = "{\"error\": " + adicional.errors().toJSON() + "}";
                logger.error(adicional.errors().toString());
                respond(errorJson).contentType("application/json").status(400);
            }
        } catch (Exception e) {
            String errorJson = "{\"error\": \"" + e.getMessage() + "\"}";
            logger.error(e.getMessage());
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
                    logger.info(adicional.toJson().toString());
                    respond(adicional.toJson().toString()).contentType("application/json").status(200);
                } else {
                    logger.error(adicional.errors().toString());
                    respond(adicional.errors().toJSON()).contentType("application/json").status(400);
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
                respond("{\"error\": \"Error al actualizar el adicional\"}")
                        .contentType("application/json").status(500);
            }
        } else {
            logger.error("Adicional no encontrado. ON UPDATE");
            respond("{\"error\": \"Adicional no encontrado\"}").contentType("application/json").status(404);
        }
    }

    // Obtener todos los adicionales
    @GET
    public void index() {
        List<Adicional> adicionales = Adicional.findAll();
        JSONArray jsonArray = new JSONArray();
        adicionales.forEach(adicional -> jsonArray.put(adicional.toJson()));
        logger.info(jsonArray.toString());
        respond(jsonArray.toString()).contentType("application/json");
    }

    // Obtener un adicional por ID
    @GET
    public void show() {
        int id = Integer.parseInt(param("id"));
        Adicional adicional = Adicional.findById(id);

        if (adicional != null) {
            logger.info(adicional.toJson().toString());
            respond(adicional.toJson().toString()).contentType("application/json").status(200);
        } else {
            logger.error("Adicional no encontrado. ON SHOW");
            respond("{\"error\": \"Adicional no encontrado\"}").contentType("application/json").status(404);
        }
    }

    // Eliminar un adicional
    @DELETE
    public void delete() {
        int id = Integer.parseInt(param("id"));
        Adicional adicional = Adicional.findById(id);
        if (adicional != null) {
            logger.info("Adicional eliminado correctamente " +  adicional.toString());
            adicional.delete();
            respond("{\"message\": \"Adicional eliminado correctamente\"}")
                    .contentType("application/json").status(200);
        } else {
            logger.error("Adicional no encontrado. ON DELETE");
            respond("{\"error\": \"Adicional no encontrado\"}").contentType("application/json").status(404);
        }
    }
}
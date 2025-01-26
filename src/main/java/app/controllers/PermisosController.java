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
import org.json.JSONObject;

import app.models.Permiso;

public class PermisosController extends AppController {

private static final Logger logger = LogManager.getLogger(PermisosController.class);

    @POST
    public void create(Permiso p) {
        try {
            // Leer datos del cuerpo de la solicitud
            String endpoint = p.getString("endpoint");
            String tipo = p.getString("tipo");
            Integer rolId = p.getInteger("rol_id");

            // Validar los datos obligatorios
            if (endpoint == null || tipo == null || rolId == null) {
                logger.error("Los campos endpoint, tipo y rol_id son obligatorios para el permiso. ON CREATE");
                respond("{\"error\": \"Los campos endpoint, tipo y rol_id son obligatorios.\"}")
                        .contentType("application/json")
                        .status(400);
                return;
            }

            // Validar que el tipo sea 'LECTURA' o 'ESCRITURA'
            if (!tipo.equals("LECTURA") && !tipo.equals("ESCRITURA")) {
                logger.error("El campo tipo debe ser 'LECTURA' o 'ESCRITURA' para el permiso. ON CREATE");
                respond("{\"error\": \"El campo tipo debe ser 'LECTURA' o 'ESCRITURA'.\"}")
                        .contentType("application/json")
                        .status(400);
                return;
            }

            // Crear un nuevo permiso
            Permiso permiso = new Permiso();
            permiso.set("endpoint", endpoint);
            permiso.set("tipo", tipo);
            permiso.set("rol_id", rolId);

            // Guardar en la base de datos
            if (permiso.save()) {
                logger.info("Permiso creado: " + permiso.toString());
                respond(permiso.toJson().toString())
                        .contentType("application/json")
                        .status(201);
            } else {
                logger.error(permiso.errors().toString());
                respond("{\"error\": " + permiso.errors().toJSON() + "}")
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
    public void obtenerPermisos() {
        String tipoUsuarioParam = param("tipo_usuario");

        if (tipoUsuarioParam == null || tipoUsuarioParam.isEmpty()) {
            logger.error("El tipo de usuario es requerido. ON obtenerPermisos");
            respond("{\"error\": \"El tipo de usuario es requerido\"}")
                    .contentType("application/json").status(400);
            return;
        }

        int tipoUsuario;
        try {
            tipoUsuario = Integer.parseInt(tipoUsuarioParam);
        } catch (NumberFormatException e) {
            logger.error("El tipo de usuario debe ser un número entero válido. ON obtenerPermisos");
            respond("{\"error\": \"El tipo de usuario debe ser un número entero válido\"}")
                    .contentType("application/json").status(400);
            return;
        }

        if (tipoUsuario <= 0) {
            logger.error("El tipo de usuario debe ser mayor que 0. ON obtenerPermisos");
            respond("{\"error\": \"El tipo de usuario debe ser mayor que 0\"}")
                    .contentType("application/json").status(400);
            return;
        }

        // Obtener los permisos asociados al tipo de usuario
        List<Permiso> permisos = Permiso.where("rol_id = ?", tipoUsuario);

        if (permisos.isEmpty()) {
            logger.error("No se encontraron permisos para este tipo de usuario. ON obtenerPermisos");
            respond("{\"error\": \"No se encontraron permisos para este tipo de usuario\"}")
                    .contentType("application/json").status(404);
            return;
        }

        // Construir la respuesta en JSON
        JSONObject responseJson = new JSONObject();
        responseJson.put("rol_id", tipoUsuario);

        JSONArray permisosArray = new JSONArray();
        for (Permiso permiso : permisos) {
            permisosArray.put(permiso.toJson());
        }
        responseJson.put("permisos", permisosArray);
        logger.info(responseJson.toString());
        respond(responseJson.toString()).contentType("application/json").status(200);
    }

    // Obtener todos los descuentos
    @GET
    public void index() {
        List<Permiso> permisos = Permiso.findAll();
        JSONArray jsonArray = new JSONArray();
        permisos.forEach(permiso -> jsonArray.put(permiso.toBeautifyJson()));
        logger.info(jsonArray.toString());
        respond(jsonArray.toString()).contentType("application/json");
    }

    @PUT
    public void update(Permiso permisoRequest) {
        try {
            // Leer el parámetro 'id' desde la solicitud
            String idParam = param("id");

            if (idParam == null || idParam.isEmpty()) {
                logger.error("El ID del permiso es requerido. ON UPDATE");
                respond("{\"error\": \"El ID del permiso es requerido\"}")
                        .contentType("application/json").status(400);
                return;
            }

            int id;
            try {
                id = Integer.parseInt(idParam);
            } catch (NumberFormatException e) {
                logger.error("El ID debe ser un número entero válido. ON UPDATE");
                respond("{\"error\": \"El ID debe ser un número entero válido\"}")
                        .contentType("application/json").status(400);
                return;
            }

            Permiso permiso = Permiso.findById(id);

            if (permiso == null) {
                logger.error("Permiso no encontrado. ON UPDATE");
                respond("{\"error\": \"Permiso no encontrado\"}")
                        .contentType("application/json").status(404);
                return;
            }

            // Leer los campos del cuerpo de la solicitud y validar
            String endpoint = permisoRequest.getString("endpoint");
            String tipo = permisoRequest.getString("tipo");
            Integer rolId = permisoRequest.getInteger("rol_id");

            if (endpoint != null && !endpoint.isEmpty()) {
                permiso.set("endpoint", endpoint);
            }
            if (tipo != null && !tipo.isEmpty()) {
                if (!tipo.equals("LECTURA") && !tipo.equals("ESCRITURA")) {
                    logger.error("El campo tipo debe ser 'LECTURA' o 'ESCRITURA'. ON UPDATE");
                    respond("{\"error\": \"El campo tipo debe ser 'LECTURA' o 'ESCRITURA'.\"}")
                            .contentType("application/json").status(400);
                    return;
                }
                permiso.set("tipo", tipo);
            }
            if (rolId != null) {
                permiso.set("rol_id", rolId);
            }

            if (permiso.save()) {
                logger.info("Permiso actualizado:" + permiso.toString());
                respond(permiso.toJson().toString())
                        .contentType("application/json").status(200);
            } else {
                logger.error(permiso.errors().toString());
                respond("{\"error\": " + permiso.errors().toJSON() + "}")
                        .contentType("application/json")
                        .status(400);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            respond("{\"error\": \"Error interno del servidor: " + e.getMessage() + "\"}")
                    .contentType("application/json").status(500);
        }
    }

    @DELETE
    public void delete() {
        try {
            // Leer el parámetro 'id' desde la solicitud
            String idParam = param("id");

            if (idParam == null || idParam.isEmpty()) {
                logger.error("El ID del permiso es requerido. ON DELETE");
                respond("{\"error\": \"El ID del permiso es requerido\"}")
                        .contentType("application/json").status(400);
                return;
            }

            int id;
            try {
                id = Integer.parseInt(idParam);
            } catch (NumberFormatException e) {
                logger.error("El ID debe ser un número entero válido. ON DELETE");
                respond("{\"error\": \"El ID debe ser un número entero válido\"}")
                        .contentType("application/json").status(400);
                return;
            }

            Permiso permiso = Permiso.findById(id);

            if (permiso == null) {
                logger.error("Permiso no encontrado. ON DELETE");
                respond("{\"error\": \"Permiso no encontrado\"}")
                        .contentType("application/json").status(404);
                return;
            }

            logger.info("Permiso eliminado correctamente: " + permiso.toString());
            permiso.delete();
            respond("{\"message\": \"Permiso eliminado correctamente\"}")
                    .contentType("application/json")
                    .status(200);
        } catch (Exception e) {
            logger.error(e.getMessage());
            respond("{\"error\": \"Error interno del servidor: " + e.getMessage() + "\"}")
                    .contentType("application/json").status(500);
        }
    }
}
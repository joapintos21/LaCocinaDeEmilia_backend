package app.controllers;

import java.util.List;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.DELETE;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.PUT;
import org.json.JSONArray;
import org.json.JSONObject;

import app.models.Permiso;

public class PermisosController extends AppController {

    @POST
    public void create(Permiso p) {
        try {
            // Leer datos del cuerpo de la solicitud
            String endpoint = p.getString("endpoint");
            String tipo = p.getString("tipo");
            Integer rolId = p.getInteger("rol_id");

            // Validar los datos obligatorios
            if (endpoint == null || tipo == null || rolId == null) {
                respond("{\"error\": \"Los campos endpoint, tipo y rol_id son obligatorios.\"}")
                        .contentType("application/json")
                        .status(400);
                return;
            }

            // Validar que el tipo sea 'LECTURA' o 'ESCRITURA'
            if (!tipo.equals("LECTURA") && !tipo.equals("ESCRITURA")) {
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
                // Responder con el permiso creado
                respond(permiso.toJson().toString())
                        .contentType("application/json")
                        .status(201);
            } else {
                // Responder con los errores de validación
                respond("{\"error\": " + permiso.errors().toJSON() + "}")
                        .contentType("application/json")
                        .status(400);
            }
        } catch (Exception e) {
            // Manejar errores inesperados
            respond("{\"error\": \"Error interno del servidor: " + e.getMessage() + "\"}")
                    .contentType("application/json")
                    .status(500);
        }
    }

    @GET
    public void obtenerPermisos() {
        String tipoUsuarioParam = param("tipo_usuario");

        if (tipoUsuarioParam == null || tipoUsuarioParam.isEmpty()) {
            respond("{\"error\": \"El tipo de usuario es requerido\"}")
                    .contentType("application/json").status(400);
            return;
        }

        int tipoUsuario;
        try {
            tipoUsuario = Integer.parseInt(tipoUsuarioParam);
        } catch (NumberFormatException e) {
            respond("{\"error\": \"El tipo de usuario debe ser un número entero válido\"}")
                    .contentType("application/json").status(400);
            return;
        }

        if (tipoUsuario <= 0) {
            respond("{\"error\": \"El tipo de usuario debe ser mayor que 0\"}")
                    .contentType("application/json").status(400);
            return;
        }

        // Obtener los permisos asociados al tipo de usuario
        List<Permiso> permisos = Permiso.where("rol_id = ?", tipoUsuario);

        if (permisos.isEmpty()) {
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

        respond(responseJson.toString()).contentType("application/json").status(200);
    }

    // Obtener todos los descuentos
    @GET
    public void index() {
        List<Permiso> permisos = Permiso.findAll();
        JSONArray jsonArray = new JSONArray();
        permisos.forEach(permiso -> jsonArray.put(permiso.toBeautifyJson()));
        respond(jsonArray.toString()).contentType("application/json");
    }

    @PUT
    public void update(Permiso permisoRequest) {
        try {
            // Leer el parámetro 'id' desde la solicitud
            String idParam = param("id");

            if (idParam == null || idParam.isEmpty()) {
                respond("{\"error\": \"El ID del permiso es requerido\"}")
                        .contentType("application/json").status(400);
                return;
            }

            int id;
            try {
                id = Integer.parseInt(idParam);
            } catch (NumberFormatException e) {
                respond("{\"error\": \"El ID debe ser un número entero válido\"}")
                        .contentType("application/json").status(400);
                return;
            }

            Permiso permiso = Permiso.findById(id);

            if (permiso == null) {
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
                    respond("{\"error\": \"El campo tipo debe ser 'LECTURA' o 'ESCRITURA'.\"}")
                            .contentType("application/json").status(400);
                    return;
                }
                permiso.set("tipo", tipo);
            }
            if (rolId != null) {
                permiso.set("rol_id", rolId);
            }

            // Guardar cambios en la base de datos
            if (permiso.save()) {
                respond(permiso.toJson().toString())
                        .contentType("application/json").status(200);
            } else {
                respond("{\"error\": " + permiso.errors().toJSON() + "}")
                        .contentType("application/json")
                        .status(400);
            }
        } catch (Exception e) {
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
                respond("{\"error\": \"El ID del permiso es requerido\"}")
                        .contentType("application/json").status(400);
                return;
            }

            int id;
            try {
                id = Integer.parseInt(idParam);
            } catch (NumberFormatException e) {
                respond("{\"error\": \"El ID debe ser un número entero válido\"}")
                        .contentType("application/json").status(400);
                return;
            }

            Permiso permiso = Permiso.findById(id);

            if (permiso == null) {
                respond("{\"error\": \"Permiso no encontrado\"}")
                        .contentType("application/json").status(404);
                return;
            }

            permiso.delete();
            respond("{\"message\": \"Permiso eliminado correctamente\"}")
                    .contentType("application/json")
                    .status(200);
        } catch (Exception e) {
            respond("{\"error\": \"Error interno del servidor: " + e.getMessage() + "\"}")
                    .contentType("application/json").status(500);
        }
    }
}
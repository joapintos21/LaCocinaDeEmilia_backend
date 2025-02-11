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
import org.json.JSONObject;

import app.models.CuponNutricionista;
import app.models.Descuento;
import app.models.Nutricionista;
import app.models.TipoDescuento;
import app.models.Usuario;

public class CuponesController extends AppController {
    private static final Logger logger = LogManager.getLogger(CuponesController.class);

    // Crear una nueva categoría
    @POST
    public void create() {
        JSONObject body = new JSONObject(getRequestString());
        Integer tipoNombre = body.getInt("tipo_id");
        TipoDescuento tipoDescuento = TipoDescuento.findFirst("id = ?", tipoNombre);
        double valor = body.getDouble("valor");
        String codigo = body.getString("codigo");
        int nutricionista_id = body.getInt("nutricionista_id");
        // Valido si existe el nutricionista
        Usuario user = Usuario.findById(nutricionista_id);
        if (user == null) {
            logger.error("Nutricionista inválido. ON CREATE");
            respond("{\"error\": \"Nutricionista id inválido\"}").contentType("application/json").status(400);
            return;
        }
        Nutricionista nutricionista = (Nutricionista) Nutricionista.findBySQL(
                "SELECT n.id FROM gestion_usuarios.Nutricionista n " +
                        "JOIN gestion_usuarios.Cliente c ON n.cliente_id = c.id " +
                        "WHERE c.usuario_id = ?",
                nutricionista_id).getFirst();
        // Valido si existe un cuponNutricionista para nutricionista_id
        CuponNutricionista cuponExistente = CuponNutricionista.findFirst("nutricionista_id = ?", nutricionista.getId());
        if (cuponExistente != null) {
            logger.error("Ya existe un cupon para este nutricionista. ON CREATE");
            respond("{\"error\": \"Ya existe un cupon para este nutricionista\"}").contentType("application/json")
                    .status(400);
            return;
        }
        Descuento desc = new Descuento();
        if (tipoDescuento == null) {
            logger.error("Tipo de descuento inválido. ON CREATE");
            respond("{\"error\": \"Tipo de descuento inválido\"}").contentType("application/json").status(400);
            return;
        }
        desc.set("tipo_id", tipoDescuento.getId());
        desc.set("valor", valor);

        // Convertir las fechas a java.sql.Date
        String fechaInicio = body.getString("fechainicio");
        String fechaFin = body.getString("fechafin");

        desc.set("fechainicio", Date.valueOf(fechaInicio)); // Convertir a java.sql.Date

        if (fechaFin != null && !fechaFin.isEmpty()) {
            desc.set("fechafin", Date.valueOf(fechaFin)); // Convertir a java.sql.Date
        } else {
            desc.set("fechafin", null);
        }
        desc.set("descripcion", "Cupon Nutricionista");
        if (desc.saveIt()) {
            CuponNutricionista cupon = new CuponNutricionista();
            cupon.set("descuento_id", desc.get("id"));
            cupon.set("codigo", codigo);
            cupon.set("nutricionista_id", nutricionista.getId());
            if (cupon.saveIt()) {
                logger.info(cupon.toJson().toString());
                respond(cupon.toJson().toString()).contentType("application/json").status(200);
            } else {
                logger.error(cupon.errors().toString());
                respond(cupon.errors().toJSON()).contentType("application/json").status(400);
            }
        } else {
            logger.error(desc.errors().toString());
            respond(desc.errors().toJSON()).contentType("application/json").status(400);
        }
    }

    // Actualizar un cupon
    @PUT
    public void update() {
        JSONObject body = new JSONObject(getRequestString());
        int id = Integer.parseInt(param("id"));
        CuponNutricionista cupon = CuponNutricionista.findById(id);
        if (cupon == null) {
            logger.info("Cupon no encontrado. ON DELETE");
            respond("{\"error\": \"Cupon no encontrado\"}").contentType("application/json").status(404);
            return;
        }
        Descuento descuento = Descuento.findById(cupon.get("descuento_id"));
        try {
            if (body.has("codigo") && !body.getString("codigo").isEmpty()) {
                cupon.set("codigo", body.getString("codigo"));
            }
            // Obtener el tipo de descuento
            Integer tipoId = body.getInt("tipo_id");
            TipoDescuento tipoDescuento = TipoDescuento.findFirst("id = ?", tipoId);
            if (tipoDescuento == null) {
                logger.error("Tipo de descuento inválido. ON UPDATE: " + descuento.toString());
                respond("{\"error\": \"Tipo de descuento inválido\"}").contentType("application/json").status(400);
                return;
            }
            if (body.has("tipo_id")) {
                descuento.set("tipo_id", tipoDescuento.getId());
            }
            if (body.has("valor")) {
                descuento.set("valor", body.get("valor"));
            }
            // Convertir las fechas a java.sql.Date
            String fechaInicio = body.getString("fechainicio");
            String fechaFin = body.getString("fechafin");

            descuento.set("fechainicio", Date.valueOf(fechaInicio)); // Convertir a java.sql.Date

            if (fechaFin != null && !fechaFin.isEmpty()) {
                descuento.set("fechafin", Date.valueOf(fechaFin)); // Convertir a java.sql.Date
            } else {
                descuento.set("fechafin", null);
            }

            // Guardar el descuento actualizado
            if (descuento.saveIt() && cupon.saveIt()) {
                logger.info(cupon.toJson().toString());
                respond(cupon.toJson().toString()).contentType("application/json").status(200);
            } else {
                logger.error(cupon.errors().toString());
                respond(cupon.errors().toJSON()).contentType("application/json").status(400);
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

    }

    // Obtener todas las categorías
    @GET
    public void index() {
        List<CuponNutricionista> cupones = CuponNutricionista.findAll();
        JSONArray jsonArray = new JSONArray();
        cupones.forEach(cupon -> jsonArray.put(cupon.toJson()));
        logger.info(jsonArray.toString());
        respond(jsonArray.toString()).contentType("application/json");
    }

    // Obtener una categoría por ID
    @GET
    public void show() {
        int id = Integer.parseInt(param("id"));
        CuponNutricionista cupon = CuponNutricionista.findById(id);
        if (cupon != null) {
            logger.info(cupon.toString());
            respond(cupon.toJson().toString()).contentType("application/json").status(200);
        } else {
            logger.info("Cupon no encontrado. ON SHOW");
            respond("{\"error\": \"Cupon no encontrado\"}").contentType("application/json").status(404);
        }
    }

    // Eliminar una categoría
    @DELETE
    public void delete() {
        int id = Integer.parseInt(param("id"));
        CuponNutricionista cupon = CuponNutricionista.findById(id);
        if (cupon != null) {
            logger.info("Cupon eliminado correctamente: " + cupon.toString());
            Descuento descuento = Descuento.findById(cupon.get("descuento_id"));
            if (descuento != null) {
                descuento.delete();
            }
            respond("{\"error\": \"Cupon eliminado correctamente.\"}").contentType("application/json").status(200);
        } else {
            logger.info("Cupon no encontrado. ON DELETE");
            respond("{\"error\": \"Cupon no encontrado\"}").contentType("application/json").status(404);
        }
    }

    // Validar un cupon
    @GET
    public void validar() {
        String codigoCupon = param("codigo");
        CuponNutricionista cupon = CuponNutricionista.findFirst("codigo = ?", codigoCupon);
        if (cupon != null) {
            logger.info(cupon.toString());
            respond(cupon.toJson().toString()).contentType("application/json").status(200);
        } else {
            logger.info("Cupon inválido.");
            respond("{\"error\": \"Cupon inválido\"}").contentType("application/json").status(404);
        }
    }
}

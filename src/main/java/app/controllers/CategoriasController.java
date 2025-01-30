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

import app.models.Categoria;
import app.models.Producto;

public class CategoriasController extends AppController {
    private static final Logger logger = LogManager.getLogger(CategoriasController.class);

    // Crear una nueva categoría
    @POST
    public void create() {
        JSONObject body = new JSONObject(getRequestString());
        String descripcion = body.getString("descripcion");
        String titulo = body.getString("titulo");

        Categoria categoria = new Categoria();
        categoria.set("descripcion", descripcion);
        categoria.set("titulo", titulo);
        try {
            if (categoria.saveIt()) {
                logger.info(categoria.toJson());
                respond(categoria.toJson().toString()).contentType("application/json").status(200);
            } else {
                String errorJson = String.format("{\"error\": %s}", categoria.errors().toJSON());
                logger.error(categoria.errors().toString());
                respond(errorJson).contentType("application/json").status(400);
            }
        } catch (Exception e) {
            String errorJson = "{\"error\": \"" + e.getMessage() + "\"}";
            logger.error(e.getMessage());
            respond(errorJson).contentType("application/json").status(500);
        }
    }

    // Actualizar una categoría
    @PUT
    public void update(Categoria newCategoria) {
        int id = Integer.parseInt(param("id"));
        Categoria categoria = Categoria.findById(id);

        if (categoria != null) {
            try {
                categoria.fromMap(newCategoria.toMap());
                if (categoria.saveIt()) {
                    logger.info(categoria.toJson().toString());
                    respond(categoria.toJson().toString()).contentType("application/json").status(200);
                } else {
                    logger.error(categoria.errors().toJSON());
                    respond(categoria.errors().toJSON()).contentType("application/json").status(400);
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
                respond("Error al actualizar la categoría").contentType("application/json").status(500);
            }
        } else {
            logger.error("Categoría no encontrada. ON UPDATE");
            respond("Categoría no encontrada").contentType("application/json").status(404);
        }
    }

    // Obtener todas las categorías
    @GET
    public void index() {
        List<Categoria> categorias = Categoria.findAll();
        JSONArray jsonArray = new JSONArray();
        categorias.forEach(categoria -> jsonArray.put(categoria.toJson()));
        logger.info(jsonArray.toString());
        respond(jsonArray.toString()).contentType("application/json");
    }

    // Obtener una categoría por ID
    @GET
    public void show() {
        int id = Integer.parseInt(param("id"));
        Categoria categoria = Categoria.findById(id);

        if (categoria != null) {
            logger.info(categoria.toJson().toString());
            respond(categoria.toJson().toString()).contentType("application/json").status(200);
        } else {
            logger.error("Categoría no encontrada. ON SHOW");
            respond("Categoría no encontrada").contentType("application/json").status(404);
        }
    }

    // Eliminar una categoría
    @DELETE
    public void delete() {
        int id = Integer.parseInt(param("id"));
        Categoria categoria = Categoria.findById(id);
        if (categoria != null) {
            logger.info("Categoría eliminada correctamente: " + categoria.toString());
            categoria.delete();
            respond("Categoría eliminada correctamente").status(200);
        } else {
            logger.info("Categoría no encontrada. ON DELETE");
            respond("Categoría no encontrada").status(404);
        }
    }

    // Método para listar todos los productos de una categoría específica
    @GET
    public void listarProductosCategoria() {
        int categoriaId = Integer.parseInt(param("id"));
        Categoria categoria = Categoria.findById(categoriaId);

        if (categoria != null) {
            // Obtener los productos asociados a la categoría
            List<Producto> productos = categoria.getProductos();
            JSONArray productosArray = new JSONArray();

            for (Producto producto : productos) {
                JSONObject productoJson = new JSONObject();
                productoJson.put("id", producto.get("id"));
                productoJson.put("descripcion", producto.get("descripcion"));
                productoJson.put("precio", producto.get("precio"));
                productoJson.put("visible", producto.get("visible"));
                productosArray.put(productoJson);
            }

            // Crear el JSON de respuesta con la información de la categoría y sus productos
            JSONObject responseJson = new JSONObject();
            responseJson.put("categoria", categoria.get("titulo"));
            responseJson.put("productos", productosArray);
            logger.info(responseJson.toString());
            respond(responseJson.toString()).contentType("application/json").status(200);
        } else {
            logger.error("Categoría no encontrada. ON listarProductosCategoria");
            respond("{\"error\": \"Categoría no encontrada\"}").contentType("application/json").status(404);
        }
    }
}

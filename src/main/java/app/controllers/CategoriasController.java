package app.controllers;

import app.models.Categoria;
import app.models.Producto;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.DELETE;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.PUT;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class CategoriasController extends AppController {

    // Crear una nueva categoría
    @POST
    public void create(Categoria categoria) {
        try {
            if (categoria.save()) {
                respond(categoria.toJson().toString()).contentType("application/json").status(200);
            } else {
                String errorJson = "{\"error\": " + categoria.errors().toJSON() + "}";
                respond(errorJson).contentType("application/json").status(400);
            }
        } catch (Exception e) {
            String errorJson = "{\"error\": \"" + e.getMessage() + "\"}";
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
                    respond(categoria.toJson().toString()).contentType("application/json").status(200);
                } else {
                    respond(categoria.errors().toJSON()).contentType("application/json").status(400);
                }
            } catch (Exception e) {
                respond("Error al actualizar la categoría").contentType("application/json").status(500);
            }
        } else {
            respond("Categoría no encontrada").contentType("application/json").status(404);
        }
    }

    // Obtener todas las categorías
    @GET
    public void index() {
        List<Categoria> categorias = Categoria.findAll();
        JSONArray jsonArray = new JSONArray();
        categorias.forEach(categoria -> jsonArray.put(categoria.toJson()));
        respond(jsonArray.toString()).contentType("application/json");
    }

    // Obtener una categoría por ID
    @GET
    public void show() {
        int id = Integer.parseInt(param("id"));
        Categoria categoria = Categoria.findById(id);

        if (categoria != null) {
            respond(categoria.toJson().toString()).contentType("application/json").status(200);
        } else {
            respond("Categoría no encontrada").contentType("application/json").status(404);
        }
    }

    // Eliminar una categoría
    @DELETE
    public void delete() {
        int id = Integer.parseInt(param("id"));
        Categoria categoria = Categoria.findById(id);
        if (categoria != null) {
            categoria.delete();
            respond("Categoría eliminada correctamente").status(200);
        } else {
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

            respond(responseJson.toString()).contentType("application/json").status(200);
        } else {
            respond("{\"error\": \"Categoría no encontrada\"}").contentType("application/json").status(404);
        }
    }
}

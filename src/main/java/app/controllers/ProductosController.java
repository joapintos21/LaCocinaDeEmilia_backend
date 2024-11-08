package app.controllers;

import app.models.Producto;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.DELETE;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.PUT;
import org.json.JSONArray;

import java.util.List;

public class ProductosController extends AppController {

    // Crear un nuevo producto
    @POST
    public void create(Producto producto) {
        try {
            if (producto.save()) {
                respond(producto.toJson().toString()).contentType("application/json").status(200);
            } else {
                String errorJson = "{\"error\": " + producto.errors().toJSON() + "}";
                respond(errorJson).contentType("application/json").status(400);
            }
        } catch (Exception e) {
            String errorJson = "{\"error\": \"" + e.getMessage() + "\"}";
            respond(errorJson).contentType("application/json").status(500);
        }
    }

    // Actualizar un producto
    @PUT
    public void update(Producto newProduct) {
        int id = Integer.parseInt(param("id"));
        Producto producto = Producto.findById(id);

        if (producto != null) {
            try {
                producto.fromMap(newProduct.toMap());
                if (producto.saveIt()) {
                    respond(producto.toJson().toString()).contentType("application/json").status(200);
                } else {
                    respond(producto.errors().toJSON()).contentType("application/json").status(400);
                }
            } catch (Exception e) {
                respond("Error al actualizar el producto").contentType("application/json").status(500);
            }
        } else {
            respond("Producto no encontrado").contentType("application/json").status(404);
        }
    }

    // Obtener todos los productos
    @GET
    public void index() {
        List<Producto> productos = Producto.findAll();
        JSONArray jsonArray = new JSONArray();
        productos.forEach(producto -> jsonArray.put(producto.toJson()));
        respond(jsonArray.toString()).contentType("application/json");
    }

    // Obtener un producto por ID
    @GET
    public void show() {
        int id = Integer.parseInt(param("id"));
        Producto producto = Producto.findById(id);

        if (producto != null) {
            respond(producto.toJson().toString()).contentType("application/json").status(200);
        } else {
            respond("Producto no encontrado").contentType("application/json").status(404);
        }
    }

    // Eliminar un producto
    @DELETE
    public void delete() {
        int id = Integer.parseInt(param("id"));
        Producto producto = Producto.findById(id);
        if (producto != null) {
            producto.delete();
            respond("Producto eliminado correctamente").status(200);
        } else {
            respond("Producto no encontrado").status(404);
        }
    }
}

package app.controllers;

import app.config.FirebaseConfig;
import app.models.Adicional;
import app.models.DescuentoProducto;
import app.models.Producto;
import app.services.FirebaseStorageService;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.DELETE;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.PUT;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class ProductosController extends AppController {

    @POST
    public void create() {
        try {
            FirebaseConfig.initializeFirebase(); // Asegura que Firebase está inicializado

            JSONObject body = new JSONObject(getRequestString());
            String descripcion = body.getString("descripcion");
            double precio = body.getDouble("precio");
            int categoriaId = body.getInt("categoria_id");
            String imageUrl = body.optString("imagen", null); // Recibimos solo la URL, no Base64

            Producto producto = new Producto();
            producto.set("descripcion", descripcion);
            producto.set("precio", precio);
            producto.set("categoria_id", categoriaId);
            if (imageUrl != null) {
                producto.set("imagen", imageUrl); // Guardamos solo la URL en la BD
            }

            if (producto.saveIt()) {
                respond(producto.toJson().toString()).contentType("application/json").status(201);
            } else {
                respond(producto.errors().toJSON()).contentType("application/json").status(400);
            }
        } catch (Exception e) {
            respond("{\"error\": \"" + e.getMessage() + "\"}").contentType("application/json").status(500);
        }
    }

    // Actualizar un producto
    @PUT
    public void update() {
        try {
            JSONObject body = new JSONObject(getRequestString()); // Recibir JSON

            int id = body.getInt("id");
            Producto producto = Producto.findById(id);

            if (producto != null) {
                if (body.has("descripcion") && !body.getString("descripcion").isEmpty()) {
                    producto.set("descripcion", body.getString("descripcion"));
                }
                if (body.has("precio")) {
                    producto.set("precio", body.getDouble("precio"));
                }
                if (body.has("categoria_id")) {
                    producto.set("categoria_id", body.getInt("categoria_id"));
                }
                if (body.has("visible")) {
                    producto.set("visible", body.getBoolean("visible"));
                }

                // Actualizar la imagen solo si se proporciona una nueva URL
                if (body.has("imagen") && !body.getString("imagen").isEmpty()) {
                    String imageUrl = body.getString("imagen");
                    producto.set("imagen", imageUrl); // Guardamos solo la URL en PostgreSQL
                }

                if (producto.saveIt()) {
                    respond(producto.toJson().toString()).contentType("application/json").status(200);
                } else {
                    respond(producto.errors().toJSON()).contentType("application/json").status(400);
                }
            } else {
                respond("{\"error\": \"Producto no encontrado\"}").contentType("application/json").status(404);
            }
        } catch (Exception e) {
            respond("{\"error\": \"" + e.getMessage() + "\"}").contentType("application/json").status(500);
        }
    }

    // Obtener todos los productos
    @GET
    public void index() {
        List<Producto> productos = Producto.findAll();
        JSONArray jsonArray = new JSONArray();
        productos.forEach(producto -> jsonArray.put(producto.toJson()));
        respond(jsonArray.toString()).contentType("application/json").status(200);
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

    // Endpoint para listar los adicionales de un producto específico
    @GET
    public void listarAdicionales() {
        int productoId = Integer.parseInt(param("producto_id"));
        Producto producto = Producto.findById(productoId);

        if (producto != null) {
            // Usamos una consulta con JOIN para obtener los adicionales relacionados
            List<Adicional> adicionales = Adicional.findBySQL(
                    "SELECT a.* FROM aplicacion.Adicional a " +
                            "JOIN aplicacion.Producto_Adicional pa ON a.id = pa.adicional_id " +
                            "WHERE pa.producto_id = ?",
                    productoId);

            JSONArray adicionalesJson = new JSONArray();
            adicionales.forEach(adicional -> adicionalesJson.put(adicional.toJson()));

            respond(adicionalesJson.toString()).contentType("application/json").status(200);
        } else {
            respond("{\"error\": \"Producto no encontrado\"}")
                    .contentType("application/json").status(404);
        }
    }

    @GET
    public void buscar() {
        String query = param("query"); // Obtiene el parámetro de búsqueda

        if (query == null || query.isEmpty()) {
            respond("El parámetro de búsqueda es requerido").status(400);
            return;
        }

        // Realiza la búsqueda de productos por el campo "descripcion" o "titulo"
        List<Producto> productos = Producto.where("descripcion ILIKE ?", "%" + query + "%");

        // Convierte la lista de productos a un array JSON
        JSONArray jsonArray = new JSONArray();
        for (Producto producto : productos) {
            JSONObject jsonProducto = producto.toJson();
            jsonArray.put(jsonProducto);
        }

        // Responde con el array JSON
        respond(jsonArray.toString()).contentType("application/json").status(200);
    }

    // Eliminar un producto
    @DELETE
    public void delete() {
        try {
            int id = Integer.parseInt(param("id"));
            Producto producto = Producto.findById(id);

            if (producto != null) {
                // Obtener la URL de la imagen antes de eliminar el producto
                String imageUrl = producto.getString("imagen");

                // Eliminar la imagen de Firebase
                FirebaseStorageService.eliminarArchivo(imageUrl);

                // Eliminar el producto de la base de datos
                if (producto.delete()) {
                    respond("{\"message\": \"Producto eliminado correctamente.\"}").contentType("application/json")
                            .status(200);
                } else {
                    respond("{\"error\": \"No se pudo eliminar el producto.\"}").contentType("application/json")
                            .status(400);
                }
            } else {
                respond("{\"error\": \"Producto no encontrado.\"}").contentType("application/json").status(404);
            }
        } catch (Exception e) {
            respond("{\"error\": \"" + e.getMessage() + "\"}").contentType("application/json").status(500);
        }
    }

    @POST
    public void crearDescuento() {
        try {
            JSONObject body = new JSONObject(getRequestString());
            int productoId = body.getInt("producto_id");
            int descuentoId = body.getInt("descuento_id");

            DescuentoProducto descuentoProducto = new DescuentoProducto();
            descuentoProducto.set("producto_id", productoId);
            descuentoProducto.set("descuento_id", descuentoId);

            if (descuentoProducto.saveIt()) {
                respond(descuentoProducto.toJson().toString()).contentType("application/json").status(201);
            } else {
                respond(descuentoProducto.errors().toJSON()).contentType("application/json").status(400);
            }
        } catch (Exception e) {
            respond("{\"error\": \"" + e.getMessage() + "\"}").contentType("application/json").status(500);
        }
    }
}

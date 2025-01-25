package app.controllers;

import java.util.Base64;
import java.util.List;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.DELETE;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.PUT;
import org.json.JSONArray;
import org.json.JSONObject;

import app.models.Adicional;
import app.models.AdicionalProducto;
import app.models.DescuentoProducto;
import app.models.Producto;

public class ProductosController extends AppController {

    @POST
    public void create() {
        try {
            JSONObject body = new JSONObject(getRequestString()); // Recibir JSON

            String descripcion = body.getString("descripcion");
            double precio = body.getDouble("precio");
            int categoriaId = body.getInt("categoria_id");
            boolean visible = body.getBoolean("visible");
            String imagenBase64 = body.optString("imagen", null); // Opcional

            byte[] imagenBytes = null;
            if (imagenBase64 != null && !imagenBase64.isEmpty()) {
                imagenBytes = Base64.getDecoder().decode(imagenBase64); // Convertir Base64 a bytes
            }

            Producto producto = new Producto();
            producto.set("descripcion", descripcion);
            producto.set("precio", precio);
            producto.set("categoria_id", categoriaId);
            producto.set("visible", visible);
            if (imagenBytes != null) {
                producto.set("imagen", imagenBytes); // Guardar en BYTEA
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

                // Actualizar la imagen si se proporciona
                String imagenBase64 = body.optString("imagen", null);
                if (imagenBase64 != null && !imagenBase64.isEmpty()) {
                    byte[] imagenBytes = Base64.getDecoder().decode(imagenBase64);
                    producto.set("imagen", imagenBytes);
                }

                if (producto.saveIt()) {
                    respond(producto.toJson().toString()).contentType("application/json").status(200);
                } else {
                    respond(producto.errors().toJSON()).contentType("application/json").status(400);
                }
            } else {
                respond("Producto no encontrado").contentType("application/json").status(404);
            }
        } catch (Exception e) {
            respond("Error al actualizar el producto").contentType("application/json").status(500);
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
        int id = Integer.parseInt(param("id"));
        Producto producto = Producto.findById(id);
        if (producto != null) {
            producto.delete();
            respond("Producto eliminado correctamente").status(200);
        } else {
            respond("Producto no encontrado").status(404);
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

    @POST
    public void crearAdicional() {
        try {
            JSONObject body = new JSONObject(getRequestString());
            int productoId = body.getInt("producto_id");
            int adicionalId = body.getInt("adicional_id");

            AdicionalProducto adicionalProducto = new AdicionalProducto();
            adicionalProducto.set("producto_id", productoId);
            adicionalProducto.set("adicional_id", adicionalId);

            if (adicionalProducto.saveIt()) {
                respond(adicionalProducto.toJson().toString()).contentType("application/json").status(201);
            } else {
                respond(adicionalProducto.errors().toJSON()).contentType("application/json").status(400);
            }
        } catch (Exception e) {
            respond("{\"error\": \"" + e.getMessage() + "\"}").contentType("application/json").status(500);
        }
    }
}

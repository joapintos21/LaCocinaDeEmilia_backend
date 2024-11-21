package app.controllers;

import app.models.Pedido;
import app.models.Producto;
import app.models.Pack;
import app.models.Adicional;
import app.models.PedidoProducto;
import app.models.PedidoHistorico;
import app.models.PedidoPack;
import app.models.Usuario;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.DELETE;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.PUT;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class PedidosController extends AppController {

    // Crear un nuevo pedido
    @POST
    public void create(Pedido pedido) {
        try {
            if (pedido.save()) {
                respond(pedido.toJson().toString()).contentType("application/json").status(201);
            } else {
                String errorJson = "{\"error\": " + pedido.errors().toJSON() + "}";
                respond(errorJson).contentType("application/json").status(400);
            }
        } catch (Exception e) {
            String errorJson = "{\"error\": \"" + e.getMessage() + "\"}";
            respond(errorJson).contentType("application/json").status(500);
        }
    }

    // Actualizar el estado de un pedido y registrar en el historial
    @PUT
    public void updateStatus() {
        int id = Integer.parseInt(param("id"));
        String estadoNuevo = param("estado");
        Usuario usuario = Usuario.findById(Integer.parseInt(param("usuario_id"))); // Obtener el usuario que realiza el
                                                                                   // cambio
        Pedido pedido = Pedido.findById(id);

        if (pedido != null && usuario != null) {
            String estadoAnterior = pedido.getString("estado");
            pedido.set("estado", estadoNuevo);

            if (pedido.saveIt()) {
                PedidoHistorico.registrarCambio(pedido, estadoAnterior, estadoNuevo, usuario, "Cambio de estado");
                respond(pedido.toJson().toString()).contentType("application/json").status(200);
            } else {
                respond(pedido.errors().toJSON()).contentType("application/json").status(400);
            }
        } else {
            respond("{\"error\": \"Pedido o usuario no encontrado\"}").contentType("application/json").status(404);
        }
    }

    // Obtener todos los pedidos
    @GET
    public void index() {
        List<Pedido> pedidos = Pedido.findAll();
        JSONArray jsonArray = new JSONArray();
        pedidos.forEach(pedido -> jsonArray.put(pedido.toJson()));
        respond(jsonArray.toString()).contentType("application/json");
    }

    // Obtener un pedido por ID, incluyendo sus productos, packs y adicionales
    @GET
    public void show() {
        int id = Integer.parseInt(param("id"));
        Pedido pedido = Pedido.findById(id);

        if (pedido != null) {
            // Crear un objeto JSON manualmente para el pedido
            JSONObject pedidoJson = new JSONObject();
            pedidoJson.put("id", pedido.getId());
            pedidoJson.put("cliente_id", pedido.get("cliente_id"));
            pedidoJson.put("estado", pedido.get("estado"));
            pedidoJson.put("metodo_pago", pedido.get("metodo_pago"));
            pedidoJson.put("total", pedido.get("total"));

            // Crear un array para los productos
            JSONArray productosJsonArray = new JSONArray();

            // Obtener los productos del pedido usando la tabla de relación PedidoProducto
            List<PedidoProducto> pedidoProductos = PedidoProducto.where("pedido_id = ?", pedido.getId());

            for (PedidoProducto pedidoProducto : pedidoProductos) {
                // Obtener el producto asociado
                Producto producto = Producto.findById(pedidoProducto.get("producto_id"));

                if (producto != null) {
                    // Crear un objeto JSON para el producto
                    JSONObject productoJson = new JSONObject();
                    productoJson.put("id", producto.get("id"));
                    productoJson.put("descripcion", producto.get("descripcion"));
                    productoJson.put("cantidad", pedidoProducto.get("cantidad"));
                    productoJson.put("precio", producto.get("precio"));

                    // Agregar el producto al array de productos
                    productosJsonArray.put(productoJson);
                }
            }

            // Agregar el array de productos al objeto JSON del pedido
            pedidoJson.put("productos", productosJsonArray);

            // Crear un array para los packs
            JSONArray packsJsonArray = new JSONArray();
            List<PedidoPack> pedidoPacks = PedidoPack.where("pedido_id = ?", pedido.getId());
            for (PedidoPack pedidoPack : pedidoPacks) {
                Pack pack = Pack.findById(pedidoPack.get("pack_id"));
                if (pack != null) {
                    JSONObject packJson = new JSONObject();
                    packJson.put("id", pack.get("id"));
                    packJson.put("nombre", pack.get("nombre"));
                    packJson.put("cantidad", pedidoPack.get("cantidad"));
                    packJson.put("precio", pack.get("precio"));
                    packsJsonArray.put(packJson);
                }
            }
            pedidoJson.put("packs", packsJsonArray);

            // Enviar la respuesta JSON completa
            respond(pedidoJson.toString()).contentType("application/json").status(200);
        } else {
            respond("{\"error\": \"Pedido no encontrado\"}").contentType("application/json").status(404);
        }
    }

    // Eliminar un pedido
    @DELETE
    public void delete() {
        int id = Integer.parseInt(param("id"));
        Pedido pedido = Pedido.findById(id);

        if (pedido != null) {
            pedido.delete();
            respond("{\"message\": \"Pedido eliminado correctamente\"}").contentType("application/json").status(200);
        } else {
            respond("{\"error\": \"Pedido no encontrado\"}").contentType("application/json").status(404);
        }
    }

    // Agregar un producto al pedido
    @POST
    public void addProducto() {
        int pedidoId = Integer.parseInt(param("pedido_id"));
        int productoId = Integer.parseInt(param("producto_id"));
        int cantidad = Integer.parseInt(param("cantidad"));
        Pedido pedido = Pedido.findById(pedidoId);
        Producto producto = Producto.findById(productoId);

        if (pedido != null && producto != null) {
            pedido.addProducto(producto, cantidad);
            respond("{\"message\": \"Producto agregado al pedido\"}").contentType("application/json").status(200);
        } else {
            respond("{\"error\": \"Pedido o producto no encontrado\"}").contentType("application/json").status(404);
        }
    }

    // Agregar un pack al pedido
    @POST
    public void addPack() {
        int pedidoId = Integer.parseInt(param("pedido_id"));
        int packId = Integer.parseInt(param("pack_id"));
        int cantidad = Integer.parseInt(param("cantidad"));
        Pedido pedido = Pedido.findById(pedidoId);
        Pack pack = Pack.findById(packId);

        if (pedido != null && pack != null) {
            pedido.addPack(pack, cantidad);
            respond("{\"message\": \"Pack agregado al pedido\"}").contentType("application/json").status(200);
        } else {
            respond("{\"error\": \"Pedido o pack no encontrado\"}").contentType("application/json").status(404);
        }
    }

    // Agregar un adicional a un producto específico dentro de un pedido
    @POST
    public void addAdicionalToProducto() {
        int pedidoId = Integer.parseInt(param("pedido_id"));
        int productoId = Integer.parseInt(param("producto_id"));
        int adicionalId = Integer.parseInt(param("adicional_id"));
        Pedido pedido = Pedido.findById(pedidoId);
        Producto producto = Producto.findById(productoId);
        Adicional adicional = Adicional.findById(adicionalId);

        if (pedido != null && producto != null && adicional != null) {
            pedido.addAdicionalToProducto(producto, adicional);
            respond("{\"message\": \"Adicional agregado al producto en el pedido\"}")
                    .contentType("application/json").status(200);
        } else {
            respond("{\"error\": \"Pedido, producto o adicional no encontrado\"}")
                    .contentType("application/json").status(404);
        }
    }

    @PUT
    public void finalizarPedido() {
        int pedidoId = Integer.parseInt(param("pedido_id"));
        Pedido pedido = Pedido.findById(pedidoId);

        if (pedido != null && "PENDIENTE".equals(pedido.getString("estado"))) {
            pedido.set("estado", "COMPLETADO"); // Cambiar el estado a COMPLETADO
            pedido.saveIt();
            respond("{\"message\": \"Pedido completado con éxito\"}").contentType("application/json").status(200);
        } else {
            respond("{\"error\": \"Pedido no encontrado o ya procesado\"}").contentType("application/json").status(404);
        }
    }
}
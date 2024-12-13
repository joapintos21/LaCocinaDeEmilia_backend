package app.controllers;

import app.models.Pedido;
import app.models.PedidoDetalle;
import app.models.Producto;
import app.models.Pack;
import app.models.Pago;
import app.models.Adicional;
import app.models.Cliente;
import app.models.Envio;
import app.models.EstadoPedido;
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

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.payment.Payment;

import java.math.BigDecimal;
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

    @GET
    public void obtenerPedidosUsuario() {
        int userId;
        try {
            // Obtener el userId del contexto
            int userIdFromToken = (Integer) appContext().get("userId");
            // Obtener el userId del parámetro
            userId = Integer.parseInt(param("userId"));

            // Validar que el userId del token coincida con el del parámetro
            if (userIdFromToken != userId) {
                respond("{\"error\": \"No tienes permiso para acceder a estos pedidos.\"}")
                        .contentType("application/json")
                        .status(403);
                return;
            }

        } catch (NumberFormatException e) {
            respond("{\"error\": \"El parámetro userId es inválido o no está presente.\"}")
                    .contentType("application/json")
                    .status(400);
            return;
        }
        // Obtener el cliente asociado al usuario
        Cliente cliente = Cliente.findFirst("usuario_id = ?", userId);
        if (cliente == null) {
            respond("{\"error\": \"Cliente no encontrado.\"}")
                    .contentType("application/json")
                    .status(404);
            return;
        }

        // Buscar los pedidos del cliente
        List<Pedido> pedidos = Pedido.where("cliente_id = ? and estado > 1", cliente.getId()).orderBy("id DESC");

        // Construir la respuesta JSON con los detalles de los pedidos
        JSONArray pedidosJson = new JSONArray();
        for (Pedido pedido : pedidos) {
            JSONObject pedidoJson = new JSONObject();
            pedidoJson.put("id", pedido.getId());
            pedidoJson.put("estado", pedido.get("estado"));
            pedidoJson.put("total", pedido.get("total"));
            pedidoJson.put("fechaCreacion", pedido.get("fechaCreacion"));

            // Obtener el estado de pago asociado al pedido
            Pago pago = Pago.findFirst("pedido_id = ? order by fecha_pago DESC", pedido.getId());
            if (pago != null) {
                pedidoJson.put("estado_pago", pago.get("estado_pago")); // Agrega el estado de pago
            } else {
                pedidoJson.put("estado_pago", "SIN_PAGO"); // Valor predeterminado si no hay pago registrado
            }
            // Obtener los detalles del pedido
            List<PedidoDetalle> detalles = PedidoDetalle.where("pedido_id = ?", pedido.getId());
            JSONArray detallesJson = new JSONArray();
            for (PedidoDetalle detalle : detalles) {
                JSONObject detalleJson = new JSONObject();
                detalleJson.put("descripcion", detalle.get("descripcion"));
                detalleJson.put("cantidad", detalle.get("cantidad"));
                detalleJson.put("precio_unitario", detalle.get("precio_unitario"));
                detalleJson.put("subtotal", detalle.get("subtotal"));
                detallesJson.put(detalleJson);
            }
            pedidoJson.put("detalles", detallesJson);

            // Agregar el pedido al array de pedidos
            pedidosJson.put(pedidoJson);
        }
        // Responde con los pedidos
        respond(pedidosJson.toString()).contentType("application/json").status(200);
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
        JSONObject body = new JSONObject(getRequestString());
        int pedidoId = body.getInt("pedido_id");
        int metodoPagoId = body.getInt("metodo_pago_id");
        String paymentId = body.getString("payment_id"); // Opcional
        BigDecimal comision = BigDecimal.ZERO;
        EstadoPedido estadoEnProgreso = EstadoPedido.findFirst("nombre = ?", "EN_PROGRESO");

        try {
            // Buscar el pedido
            Pedido pedido = Pedido.findFirst("id = ? AND estado = ?", pedidoId, estadoEnProgreso.getId());
            if (pedido == null) {
                respond("{\"error\": \"Pedido no encontrado\"}").contentType("application/json").status(404);
                return;
            }

            Envio envio = Envio.findFirst("pedido_id = ?", pedidoId);

            // Verificar el método de pago
            if (metodoPagoId == 1) {
                // Transferencia: Crear el pago como pendiente
                crearPago(pedido, metodoPagoId, "PENDIENTE", pedido.getBigDecimal("total"));
                pedido.set("estado", 2); // LISTO_PARA_PREPARAR
                pedido.saveIt();

            } else if (metodoPagoId == 2) {
                // Mercado Pago: Verificar el payment_id
                Pago pagoExistente = Pago.findFirst("pedido_id = ? AND metodo_pago_id = ?", pedidoId, 2);
                if (pagoExistente == null) {
                    // Si no hay pagos registrados, consultar la API de Mercado Pago
                    try {
                        PaymentClient paymentClient = new PaymentClient();
                        Payment payment = paymentClient.get(Long.parseLong(paymentId));

                        String status = payment.getStatus(); // Estado del pago
                        BigDecimal transactionAmount = payment.getTransactionAmount();
                        pedido.set("total", transactionAmount);
                        comision = new BigDecimal(5.99);
                        if ("approved".equals(status)) {
                            crearPago(pedido, metodoPagoId, "APROBADO", transactionAmount);
                            pedido.set("estado", 2); // LISTO_PARA_PREPARAR
                        } else if ("pending".equals(status) || "in_process".equals(status)) {
                            crearPago(pedido, metodoPagoId, "PENDIENTE", transactionAmount);
                            pedido.set("estado", 2); // LISTO_PARA_PREPARAR
                        } else {
                            crearPago(pedido, metodoPagoId, "RECHAZADO", transactionAmount);
                            pedido.set("estado", 1); // EN_PROGRESO
                        }
                    } catch (NumberFormatException e) {
                        // El payment_id no es un número válido
                        respond("{\"error\": \"El ID del pago no tiene un formato válido.\"}")
                                .contentType("application/json").status(400);
                        return;
                    } catch (MPApiException e) {
                        // La API de Mercado Pago devolvió un error (por ejemplo, el ID no existe)
                        respond("{\"error\": \"El ID del pago no es válido o no existe en Mercado Pago.\"}")
                                .contentType("application/json").status(404);
                        return;
                    } catch (Exception e) {
                        // Manejo genérico de errores
                        respond("{\"error\": \"Error al consultar el pago: " + e.getMessage() + "\"}")
                                .contentType("application/json").status(500);
                        return;
                    }
                }
                pedido.saveIt();
            } else {
                respond("{\"error\": \"Método de pago no válido\"}").contentType("application/json").status(400);
                return;
            }

            // Agregar detalles del pedido
            pedido.agregarDetallesPedido(comision, envio.getBigDecimal("costo"));

            // Guardar cambios en el pedido
            pedido.saveIt();

            respond(new JSONObject(pedido.toMap()).toString()).contentType("application/json").status(200);

        } catch (Exception e) {
            e.printStackTrace();
            respond("{\"error\": \"Error al finalizar el pedido: " + e.getMessage() + "\"}")
                    .contentType("application/json").status(500);
        }
    }

    @GET
    public void obtenerPedidoEnProgreso() {
        int userId = Integer.parseInt(param("userId"));
        EstadoPedido estadoEnProgreso = EstadoPedido.findFirst("nombre = ?", "EN_PROGRESO");

        if (estadoEnProgreso == null) {
            respond("{\"error\": \"Estado 'EN_PROGRESO' no encontrado\"}")
                    .contentType("application/json").status(500);
            return;
        }

        Cliente cliente = Cliente.findFirst("usuario_id = ?", userId);

        if (cliente == null) {
            respond("{\"error\": \"Cliente no encontrado\"}")
                    .contentType("application/json").status(404);
            return;
        }

        Pedido pedido = Pedido.findFirst("cliente_id = ? AND estado = ?", cliente.getId(), estadoEnProgreso.getId());

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

            // Responder con el JSON completo
            respond(pedidoJson.toString()).contentType("application/json").status(200);
        } else {
            respond("{\"error\": \"No hay pedidos en curso.\"}")
                    .contentType("application/json").status(404);
        }
    }

    private void crearPago(Pedido pedido, Integer metodoPagoId, String estadoPago, BigDecimal monto) {
        Pago pago = new Pago();
        pago.set("pedido_id", pedido.getId());
        pago.set("metodo_pago_id", metodoPagoId);
        pago.set("monto", monto != null ? monto : BigDecimal.ZERO);
        pago.set("estado_pago", estadoPago);
        pago.saveIt();
    }
}
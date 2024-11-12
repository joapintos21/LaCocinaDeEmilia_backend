package app.controllers;

import app.models.CarritoRequest;
import app.models.Pedido;
import app.models.Producto;
import app.models.PedidoProducto;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.POST;

public class CarritoController extends AppController {
    public enum EstadoPedido {
        EN_PROGRESO,
        PENDIENTE_CONFIRMACION_PAGO,
        LISTO_PARA_ENVIAR,
        ENTREGADO,
        CANCELADO
    }

    @POST
    public void agregarProductoACarrito(CarritoRequest carritoRequest) {
        int clienteId = carritoRequest.getClienteId();
        int productoId = carritoRequest.getProductoId();
        int cantidad = carritoRequest.getCantidad();

        // Verificar si ya existe un pedido en estado EN_PROGRESO para el cliente
        Pedido pedido = Pedido.findFirst("cliente_id = ? AND estado = ?::EstadoPedidoEnum", clienteId, "EN_PROGRESO");

        if (pedido == null) {
            // Crear un nuevo pedido si no existe
            pedido = new Pedido();
            pedido.set("cliente_id", clienteId);
            pedido.saveIt();
        }

        // Verificar si el producto ya está en el pedido
        PedidoProducto pedidoProducto = PedidoProducto.findFirst("pedido_id = ? AND producto_id = ?", pedido.getId(),
                productoId);

        if (pedidoProducto == null) {
            // Si el producto no está en el pedido, crear una nueva entrada
            pedidoProducto = new PedidoProducto();
            pedidoProducto.set("pedido_id", pedido.getId());
            pedidoProducto.set("producto_id", productoId);
            pedidoProducto.set("cantidad", cantidad);
        } else {
            // Si el producto ya está en el pedido, aumentar la cantidad existente
            int cantidadActual = pedidoProducto.getInteger("cantidad");
            pedidoProducto.set("cantidad", cantidadActual + cantidad);
        }
        pedidoProducto.saveIt();

        // Actualizar el total del pedido con el precio del producto
        Producto producto = Producto.findById(productoId);
        if (producto != null) {
            Double totalActual = pedido.getDouble("total");
            if (totalActual == null) {
                totalActual = 0.0;
            }
            double nuevoTotal = totalActual + producto.getDouble("precio") * cantidad;
            pedido.set("total", nuevoTotal);
            pedido.saveIt();
        }

        respond("{\"message\": \"Producto agregado al pedido\"}")
                .contentType("application/json").status(200);
    }

    @POST
    public void quitarProductoCarrito(CarritoRequest carritoRequest) {
        int clienteId = carritoRequest.getClienteId();
        int productoId = carritoRequest.getProductoId();
        int cantidadQuitar = carritoRequest.getCantidad();

        // Buscar el pedido en estado EN_PROGRESO para el cliente
        Pedido pedido = Pedido.findFirst("cliente_id = ? AND estado = ?::EstadoPedidoEnum", clienteId, "EN_PROGRESO");

        if (pedido != null) {
            // Buscar el producto en el pedido
            PedidoProducto pedidoProducto = PedidoProducto.findFirst("pedido_id = ? AND producto_id = ?",
                    pedido.getId(), productoId);

            if (pedidoProducto != null) {
                int cantidadActual = pedidoProducto.getInteger("cantidad");

                if (cantidadQuitar >= cantidadActual) {
                    // Si la cantidad a quitar es igual o mayor que la cantidad actual, eliminar el
                    // producto
                    pedidoProducto.delete();

                    // Verificar si era el único producto en el pedido
                    if (PedidoProducto.find("pedido_id = ?", pedido.getId()).isEmpty()) {
                        // Si no hay más productos, eliminar el pedido
                        pedido.delete();
                        respond("{\"message\": \"Producto eliminado y pedido vacío, pedido eliminado\"}")
                                .contentType("application/json").status(200);
                        return;
                    }
                } else {
                    // Si la cantidad a quitar es menor que la cantidad actual, disminuir la
                    // cantidad
                    pedidoProducto.set("cantidad", cantidadActual - cantidadQuitar);
                    pedidoProducto.saveIt();
                }

                // Actualizar el total del pedido después de quitar el producto o ajustar la
                // cantidad
                Producto producto = Producto.findById(productoId);
                if (producto != null) {
                    double nuevoTotal = pedido.getDouble("total") - (producto.getDouble("precio") * cantidadQuitar);
                    pedido.set("total", nuevoTotal > 0 ? nuevoTotal : 0.0); // Evitar total negativo
                    pedido.saveIt();
                }

                respond("{\"message\": \"Cantidad ajustada o producto eliminado del pedido\"}")
                        .contentType("application/json").status(200);
            } else {
                respond("{\"error\": \"Producto no encontrado en el pedido\"}")
                        .contentType("application/json").status(404);
            }
        } else {
            respond("{\"error\": \"Pedido no encontrado\"}")
                    .contentType("application/json").status(404);
        }
    }
}
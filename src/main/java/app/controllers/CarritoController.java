package app.controllers;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.POST;

import app.models.Adicional;
import app.models.CarritoRequest;
import app.models.Cliente;
import app.models.EstadoPedido;
import app.models.Pack;
import app.models.Pedido;
import app.models.PedidoPack;
import app.models.PedidoProducto;
import app.models.PedidoProductoAdicional;
import app.models.Producto;

public class CarritoController extends AppController {
    private static final Logger logger = LogManager.getLogger(CarritoController.class);

    @POST
    public void agregarProductoACarrito(CarritoRequest carritoRequest) {
        int userId = carritoRequest.getUserId();
        int productoId = carritoRequest.getProductoId();
        int cantidad = carritoRequest.getCantidad();
        int adicionalId = carritoRequest.getAdicionalId();
        int userIdFromToken = (Integer) appContext().get("userId");

        // Buscar el ID del estado 'EN_PROGRESO'
        EstadoPedido estadoEnProgreso = EstadoPedido.findFirst("nombre = ?", "EN_PROGRESO");
        if (estadoEnProgreso == null) {
            logger.error("Estado 'EN_PROGRESO' no encontrado. ON agregarProductoACarrito");
            respond("{\"error\": \"Estado 'EN_PROGRESO' no encontrado\"}")
                    .contentType("application/json").status(500);
            return;
        }

        Cliente cliente = Cliente.findFirst("usuario_id = ?", userId);
        if (cliente == null) {
            logger.error("Cliente no encontrado. ON agregarProductoACarrito");
            respond("{\"error\": \"Cliente no encontrado\"}")
                    .contentType("application/json").status(404);
            return;
        }

        Pedido pedido = Pedido.findFirst("cliente_id = ? AND estado = ?", cliente.getId(), estadoEnProgreso.getId());
        if (pedido == null) {
            // Crear un nuevo pedido si no existe
            pedido = new Pedido();
            pedido.set("cliente_id", cliente.getId());
            pedido.set("estado", estadoEnProgreso.getId());
            logger.info(pedido.toString());
            pedido.saveIt();
        }

        if (!pedido.isOwner(userIdFromToken)) {
            logger.error("Usuario logueado no es dueño de este pedido. ON agregarProductoACarrito");
            respond("{\"error\": \"No tiene permisos para realizar esta accion.\"}")
                    .contentType("application/json").status(401);
            return;
        }
        // Verificar si el producto ya está en el pedido (con o sin adicional)
        List<PedidoProducto> pedidoProductos = PedidoProducto.find("pedido_id = ? AND producto_id = ?", pedido.getId(),
                productoId);

        PedidoProducto pedidoProducto = null;

        if (adicionalId > 0) {
            // Filtrar para verificar si ya existe con el mismo adicionalId
            pedidoProducto = pedidoProductos.stream()
                    .filter(pp -> PedidoProductoAdicional.findFirst("pedido_producto_id = ? AND adicional_id = ?",
                            pp.getId(), adicionalId) != null)
                    .findFirst()
                    .orElse(null);
        } else {
            // Buscar sin adicional
            pedidoProducto = pedidoProductos.isEmpty() ? null : pedidoProductos.get(0);
        }

        if (pedidoProducto == null) {
            // Si no existe, crear un nuevo registro para PedidoProducto
            pedidoProducto = new PedidoProducto();
            pedidoProducto.set("pedido_id", pedido.getId());
            pedidoProducto.set("producto_id", productoId);
            pedidoProducto.set("cantidad", cantidad);
            logger.info(pedidoProducto.toString());
            pedidoProducto.saveIt();

            if (adicionalId > 0) {
                // Crear un nuevo registro para PedidoProductoAdicional
                PedidoProductoAdicional pedidoAdicional = new PedidoProductoAdicional();
                pedidoAdicional.set("pedido_producto_id", pedidoProducto.getId());
                pedidoAdicional.set("adicional_id", adicionalId);
                logger.info(pedidoAdicional.toString());
                pedidoAdicional.saveIt();
            }
        } else {
            // Si ya existe, incrementar la cantidad
            int cantidadActual = pedidoProducto.getInteger("cantidad");
            pedidoProducto.set("cantidad", cantidadActual + cantidad);
            logger.info(pedidoProducto.toString());
            pedidoProducto.saveIt();
        }

        // Actualizar el total del pedido con el precio del producto y adicional
        Producto producto = Producto.findById(productoId);
        Adicional adicional = Adicional.findById(adicionalId);
        if (producto != null) {
            Double totalActual = pedido.getDouble("total");
            Double costoAdicional = adicional != null ? adicional.getDouble("costoAdicional") : 0.0;
            totalActual = totalActual != null ? totalActual : 0.0;

            double nuevoTotal = totalActual + (producto.getDouble("precio") + costoAdicional) * cantidad;
            pedido.set("total", nuevoTotal);
            logger.info(pedido.toString());
            pedido.saveIt();
        }

        // Responder con un mensaje de éxito
        logger.info("Producto agregado al pedido: " + pedido.toString());
        respond("{\"message\": \"Producto agregado al pedido\", \"pedido_id\": " + pedido.getId() + "}")
                .contentType("application/json").status(200);
    }

    @POST
    public void quitarProductoCarrito(CarritoRequest carritoRequest) {
        int userId = carritoRequest.getUserId();
        int productoId = carritoRequest.getProductoId();
        int cantidadQuitar = carritoRequest.getCantidad();
        int adicionalId = carritoRequest.getAdicionalId(); // Campo para identificar el adicional

        // Buscar el estado 'EN_PROGRESO'
        EstadoPedido estadoEnProgreso = EstadoPedido.findFirst("nombre = ?", "EN_PROGRESO");
        if (estadoEnProgreso == null) {
            logger.error("Estado 'EN_PROGRESO' no encontrado. ON quitarProductoCarrito");
            respond("{\"error\": \"Estado 'EN_PROGRESO' no encontrado\"}")
                    .contentType("application/json").status(500);
            return;
        }

        Cliente cliente = Cliente.findFirst("usuario_id = ?", userId);
        if (cliente == null) {
            logger.error("Cliente no encontrado. ON quitarProductoCarrito");
            respond("{\"error\": \"Cliente no encontrado\"}")
                    .contentType("application/json").status(404);
            return;
        }

        Pedido pedido = Pedido.findFirst("cliente_id = ? AND estado = ?", cliente.getId(), estadoEnProgreso.getId());
        if (pedido == null) {
            logger.error("Pedido no encontrado. ON quitarProductoCarrito");
            respond("{\"error\": \"Pedido no encontrado\"}")
                    .contentType("application/json").status(404);
            return;
        }

        // Buscar todos los PedidoProducto relacionados al productoId
        List<PedidoProducto> pedidoProductos = PedidoProducto.find("pedido_id = ? AND producto_id = ?", pedido.getId(),
                productoId);

        if (pedidoProductos.isEmpty()) {
            logger.error("Producto no encontrado en el pedido. ON quitarProductoCarrito");
            respond("{\"error\": \"Producto no encontrado en el pedido\"}")
                    .contentType("application/json").status(404);
            return;
        }

        boolean actualizado = false;

        for (PedidoProducto pedidoProducto : pedidoProductos) {
            if (adicionalId > 0) {
                // Buscar si el adicional coincide
                PedidoProductoAdicional pedidoAdicional = PedidoProductoAdicional.findFirst(
                        "pedido_producto_id = ? AND adicional_id = ?", pedidoProducto.getId(), adicionalId);

                if (pedidoAdicional != null) {
                    // Reducir o eliminar adicional
                    int cantidadActual = pedidoProducto.getInteger("cantidad");

                    if (cantidadQuitar >= cantidadActual) {
                        // Eliminar adicional y PedidoProducto si la cantidad a quitar es mayor o igual
                        logger.info("Adicional eliminado en pedido: " + pedidoAdicional.toString()
                                + " Producto eliminado  en pedido: " + pedidoProducto.toString());
                        pedidoAdicional.delete();
                        pedidoProducto.delete();
                    } else {
                        // Reducir la cantidad y actualizar
                        pedidoProducto.set("cantidad", cantidadActual - cantidadQuitar);
                        logger.info("Cantidad reducida en pedido: " + pedidoProducto.toString());
                        pedidoProducto.saveIt();
                    }

                    // Actualizar el total del pedido
                    Producto producto = Producto.findById(productoId);
                    Adicional adicional = Adicional.findById(adicionalId);
                    double costoAdicional = adicional != null ? adicional.getDouble("costoAdicional") : 0.0;
                    double nuevoTotal = pedido.getDouble("total")
                            - (costoAdicional * Math.min(cantidadQuitar, cantidadActual))
                            - (producto.getDouble("precio") * Math.min(cantidadQuitar, cantidadActual));
                    pedido.set("total", Math.max(nuevoTotal, 0.0)); // Evitar total negativo
                    logger.info("Actualizacion de total del pedido: " + pedido.toString());
                    pedido.saveIt();

                    actualizado = true;
                    break; // Solo procesar un adicional específico
                }
            } else {
                // Manejar sin adicionales
                int cantidadActual = pedidoProducto.getInteger("cantidad");

                if (cantidadQuitar >= cantidadActual) {
                    // Eliminar el PedidoProducto
                    logger.info("Producto eliminado en pedido: " + pedidoProducto.toString());
                    pedidoProducto.delete();
                } else {
                    // Reducir la cantidad
                    pedidoProducto.set("cantidad", cantidadActual - cantidadQuitar);
                    logger.info("Cantidad reducida en pedido: " + pedidoProducto.toString());
                    pedidoProducto.saveIt();
                }

                // Actualizar el total del pedido
                Producto producto = Producto.findById(productoId);
                if (producto != null) {
                    double nuevoTotal = pedido.getDouble("total")
                            - (producto.getDouble("precio") * Math.min(cantidadQuitar, cantidadActual));
                    pedido.set("total", Math.max(nuevoTotal, 0.0)); // Evitar total negativo
                    logger.info("Actualizacion total en pedido: " + pedido.toString());
                    pedido.saveIt();
                }

                actualizado = true;
                break; // Solo procesar un producto sin adicional
            }
        }

        if (!actualizado) {
            logger.error("No se encontró el adicional o cantidad a ajustar en pedido: " + pedido.toString());
            respond("{\"error\": \"No se encontró el adicional o cantidad a ajustar\"}")
                    .contentType("application/json").status(404);
            return;
        }

        // Verificar si el pedido quedó vacío
        boolean tieneProductos = !PedidoProducto.find("pedido_id = ?", pedido.getId()).isEmpty();
        boolean tienePacks = !PedidoPack.find("pedido_id = ?", pedido.getId()).isEmpty();

        if (!tieneProductos && !tienePacks) {
            // Si no hay productos ni packs, eliminar el pedido
            logger.info("Producto eliminado y el pedido quedó vacío, se eliminó el pedido: " + pedido.toString());
            pedido.delete();
            respond("{\"message\": \"Producto eliminado y el pedido quedó vacío, se eliminó el pedido\"}")
                    .contentType("application/json").status(200);
            return;
        }
        logger.info("Producto ajustado o eliminado correctamente: " + pedido.toString());
        respond("{\"message\": \"Producto ajustado o eliminado correctamente\"}")
                .contentType("application/json").status(200);
    }

    @POST
    public void agregarPackACarrito(CarritoRequest carritoRequest) {
        int userId = carritoRequest.getUserId();
        int packId = carritoRequest.getProductoId(); // Usaremos productoId para el ID del pack en este caso
        int cantidad = carritoRequest.getCantidad();

        // Buscar el estado 'EN_PROGRESO' en la tabla EstadoPedido
        EstadoPedido estadoEnProgreso = EstadoPedido.findFirst("nombre = ?", "EN_PROGRESO");
        if (estadoEnProgreso == null) {
            logger.error("Estado 'EN_PROGRESO' no encontrado. ON agregarPackACarrito");
            respond("{\"error\": \"Estado 'EN_PROGRESO' no encontrado\"}")
                    .contentType("application/json").status(500);
            return;
        }
        Cliente c = Cliente.findFirst("usuario_id = ?", userId);
        // Buscar o crear el pedido en estado 'EN_PROGRESO' para el cliente
        Pedido pedido = Pedido.findFirst("cliente_id = ? AND estado = ?", c.getId(), estadoEnProgreso.getId());
        if (pedido == null) {
            pedido = new Pedido();
            pedido.set("cliente_id", c.getId());
            pedido.set("estado", estadoEnProgreso.getId());
            logger.info("Pedido creado: " + pedido.toString());
            pedido.saveIt();
        }

        // Buscar si el pack ya está en el pedido
        PedidoPack pedidoPack = PedidoPack.findFirst("pedido_id = ? AND pack_id = ?", pedido.getId(), packId);
        if (pedidoPack == null) {
            // Si el pack no está, agregarlo
            pedidoPack = new PedidoPack();
            pedidoPack.set("pedido_id", pedido.getId());
            pedidoPack.set("pack_id", packId);
            pedidoPack.set("cantidad", cantidad);
        } else {
            // Si el pack ya está, incrementar la cantidad
            int cantidadActual = pedidoPack.getInteger("cantidad");
            pedidoPack.set("cantidad", cantidadActual + cantidad);
        }
        logger.info("Pedido Pack guardado: " + pedidoPack.toString());
        pedidoPack.saveIt();

        // Actualizar el total del pedido con el precio del pack
        Pack pack = Pack.findById(packId);
        if (pack != null) {
            Double totalActual = pedido.getDouble("total");
            if (totalActual == null) {
                totalActual = 0.0;
            }
            double nuevoTotal = totalActual + pack.getDouble("precio") * cantidad;
            pedido.set("total", nuevoTotal);
            logger.info("Actualizacion de total del pedido pack: " + pedido.toString());
            pedido.saveIt();
        }

        // Responder con éxito
        logger.info("Pack agregado al pedido: " + pedido.toString());
        respond("{\"message\": \"Pack agregado al pedido\", \"pedido_id\": " + pedido.getId() + "}")
                .contentType("application/json").status(200);
    }

    @POST
    public void quitarPackCarrito(CarritoRequest carritoRequest) {
        int userId = carritoRequest.getUserId();
        int packId = carritoRequest.getProductoId(); // Usaremos productoId para el ID del pack en este caso
        int cantidadQuitar = carritoRequest.getCantidad();

        // Buscar el estado 'EN_PROGRESO' en la tabla EstadoPedido
        EstadoPedido estadoEnProgreso = EstadoPedido.findFirst("nombre = ?", "EN_PROGRESO");
        if (estadoEnProgreso == null) {
            logger.error("Estado 'EN_PROGRESO' no encontrado. ON quitarPackCarrito");
            respond("{\"error\": \"Estado 'EN_PROGRESO' no encontrado\"}")
                    .contentType("application/json").status(500);
            return;
        }

        // Buscar el cliente asociado al userId
        Cliente cliente = Cliente.findFirst("usuario_id = ?", userId);
        if (cliente == null) {
            logger.error("Cliente no encontrado. ON quitarPackCarrito");
            respond("{\"error\": \"Cliente no encontrado\"}")
                    .contentType("application/json").status(404);
            return;
        }

        // Buscar el pedido en estado 'EN_PROGRESO' para el cliente
        Pedido pedido = Pedido.findFirst("cliente_id = ? AND estado = ?", cliente.getId(), estadoEnProgreso.getId());
        if (pedido == null) {
            logger.error("Pedido no encontrado en estado 'EN_PROGRESO'. ON quitarPackCarrito");
            respond("{\"error\": \"Pedido no encontrado en estado 'EN_PROGRESO'\"}")
                    .contentType("application/json").status(404);
            return;
        }

        // Buscar el pack en el pedido
        PedidoPack pedidoPack = PedidoPack.findFirst("pedido_id = ? AND pack_id = ?", pedido.getId(), packId);
        if (pedidoPack == null) {
            logger.error("Pack no encontrado en el pedido. ON quitarPackCarrito");
            respond("{\"error\": \"Pack no encontrado en el pedido\"}")
                    .contentType("application/json").status(404);
            return;
        }

        int cantidadActual = pedidoPack.getInteger("cantidad");

        if (cantidadQuitar >= cantidadActual) {
            // Si la cantidad a quitar es igual o mayor que la cantidad actual, eliminar el
            // pack
            logger.info("Eliminacion de pack: " + pedidoPack.toString());
            pedidoPack.delete();

            // Verificar si era el único item en el pedido
            if (PedidoPack.find("pedido_id = ?", pedido.getId()).isEmpty() &&
                    PedidoProducto.find("pedido_id = ?", pedido.getId()).isEmpty()) {
                // Si no hay más items, eliminar el pedido
                logger.info("Pack eliminado y pedido vacío, pedido eliminado: " + pedido.toString());
                pedido.delete();
                respond("{\"message\": \"Pack eliminado y pedido vacío, pedido eliminado\"}")
                        .contentType("application/json").status(200);
                return;
            }
        } else {
            // Si la cantidad a quitar es menor que la cantidad actual, disminuir la
            // cantidad
            pedidoPack.set("cantidad", cantidadActual - cantidadQuitar);
            logger.info("Disminucion de cantidad de pedido: " + pedidoPack.toString());
            pedidoPack.saveIt();
        }

        // Actualizar el total del pedido después de quitar el pack o ajustar la
        // cantidad
        Pack pack = Pack.findById(packId);
        if (pack != null) {
            double nuevoTotal = pedido.getDouble("total") - (pack.getDouble("precio") * cantidadQuitar);
            pedido.set("total", Math.max(nuevoTotal, 0.0)); // Evitar total negativo
            logger.info("Actualizacion de total del pedido: " + pedido.toString());
            pedido.saveIt();
        }

        logger.info("Cantidad ajustada o pack eliminado del pedido: " + pedido.toString());
        respond("{\"message\": \"Cantidad ajustada o pack eliminado del pedido\"}")
                .contentType("application/json").status(200);
    }
}
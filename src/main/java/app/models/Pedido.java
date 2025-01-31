package app.models;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.javalite.activejdbc.annotations.BelongsTo;
import org.javalite.activejdbc.annotations.HasMany;
import org.json.JSONObject;

@Table("aplicacion.Pedido")
@BelongsTo(parent = Cliente.class, foreignKeyName = "cliente_id")
@HasMany(child = PedidoHistorico.class, foreignKeyName = "pedido_id")
@HasMany(child = PedidoProducto.class, foreignKeyName = "pedido_id")
@HasMany(child = PedidoPack.class, foreignKeyName = "pack_id")
@HasMany(child = Pago.class, foreignKeyName = "pedido_id")
@HasMany(child = PedidoDetalle.class, foreignKeyName = "pedido_id")
public class Pedido extends Model {

    static {
        validatePresenceOf("cliente_id");
    }

    // Formato JSON para Pedido
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", get("id"));
        jsonObject.put("cliente_id", get("cliente_id"));
        jsonObject.put("estado", get("estado"));
        jsonObject.put("metodo_pago", get("metodo_pago"));
        jsonObject.put("total", get("total"));
        jsonObject.put("fechaCreacion", get("fechaCreacion"));
        jsonObject.put("fecha_entrega", get("fecha_entrega"));
        jsonObject.put("cocinera_asignada", get("cocinera_asignada"));
        jsonObject.put("cadete_asignado", get("cadete_asignado"));
        return jsonObject;
    }

    // Agregar producto al pedido
    public void addProducto(Producto producto, int cantidad) {
        if (producto != null) {
            PedidoProducto pedidoProducto = new PedidoProducto();
            pedidoProducto.set("pedido_id", this.getId());
            pedidoProducto.set("producto_id", producto.getId());
            pedidoProducto.set("cantidad", cantidad);
            pedidoProducto.saveIt();
        }
    }

    // Agregar pack al pedido
    public void addPack(Pack pack, int cantidad) {
        if (pack != null) {
            PedidoPack pedidoPack = new PedidoPack();
            pedidoPack.set("pedido_id", this.getId());
            pedidoPack.set("pack_id", pack.getId());
            pedidoPack.set("cantidad", cantidad);
            pedidoPack.saveIt();
        }
    }

    // Agregar adicional a un producto específico en el pedido
    public void addAdicionalToProducto(Producto producto, Adicional adicional) {
        if (producto != null && adicional != null) {
            PedidoProductoAdicional pedidoProductoAdicional = new PedidoProductoAdicional();
            pedidoProductoAdicional.set("pedido_id", this.getId());
            pedidoProductoAdicional.set("producto_id", producto.getId());
            pedidoProductoAdicional.set("adicional_id", adicional.getId());
            pedidoProductoAdicional.saveIt();
        }
    }

    public List<Producto> obtenerProductos() {
        // Busca todos los registros en PedidoProducto relacionados con este pedido
        List<PedidoProducto> pedidoProductos = getAll(PedidoProducto.class);

        // Mapea los productos desde la relación PedidoProducto
        return pedidoProductos.stream()
                .map(pedidoProducto -> (Producto) Producto.findById(pedidoProducto.get("producto_id")))
                .filter(producto -> producto != null) // Filtra nulos en caso de inconsistencias en la base de datos
                .collect(Collectors.toList()); // Convierte a una lista
    }

    public List<Pack> obtenerPacks() {
        // Busca todos los registros en PedidoProducto relacionados con este pedido
        List<PedidoPack> pedidoPacks = getAll(PedidoPack.class);

        // Mapea los productos desde la relación PedidoProducto
        return pedidoPacks.stream()
                .map(pedidoPack -> (Pack) Pack.findById(pedidoPack.get("pack_id")))
                .filter(pack -> pack != null) // Filtra nulos en caso de inconsistencias en la base de datos
                .collect(Collectors.toList()); // Convierte a una lista
    }

    public void agregarDetallesPedido(BigDecimal comision, BigDecimal costoEnvio) {
        BigDecimal total = BigDecimal.ZERO;
        // Agregar detalles para los productos
        List<PedidoProducto> pedidoProductos = PedidoProducto.where("pedido_id = ?", this.getId());
        for (PedidoProducto pedidoProducto : pedidoProductos) {
            Producto producto = Producto.findById(pedidoProducto.get("producto_id"));
            if (producto != null) {
                BigDecimal precioTotal = producto.getPrecioTotalConDescuento();
                String descripcion = producto.getString("descripcion");

                // Verificar si hay un adicional asociado al PedidoProducto
                List<PedidoProductoAdicional> pedidoAdicionales = PedidoProductoAdicional.where(
                        "pedido_producto_id = ?", pedidoProducto.getId());
                for (PedidoProductoAdicional pedidoAdicional : pedidoAdicionales) {
                    Adicional adicional = Adicional.findById(pedidoAdicional.get("adicional_id"));
                    if (adicional != null) {
                        descripcion += " + " + adicional.get("descripcion");
                        precioTotal = precioTotal.add(adicional.getBigDecimal("costoAdicional"));
                    }
                }

                PedidoDetalle detalle = new PedidoDetalle();
                detalle.set("pedido_id", this.getId());
                detalle.set("descripcion", descripcion);
                detalle.set("cantidad", pedidoProducto.get("cantidad"));
                detalle.set("precio_unitario", precioTotal);
                detalle.set("subtotal", precioTotal.multiply(new BigDecimal(pedidoProducto.getInteger("cantidad"))));
                detalle.saveIt();
                total = total.add(precioTotal.multiply(new BigDecimal(pedidoProducto.getInteger("cantidad"))));
            }
        }

        // Agregar detalles para los packs
        List<PedidoPack> pedidoPacks = PedidoPack.where("pedido_id = ?", this.getId());
        for (PedidoPack pedidoPack : pedidoPacks) {
            Pack pack = Pack.findById(pedidoPack.get("pack_id"));
            if (pack != null) {
                PedidoDetalle detalle = new PedidoDetalle();
                detalle.set("pedido_id", this.getId());
                detalle.set("descripcion", pack.get("nombre"));
                detalle.set("cantidad", pedidoPack.get("cantidad"));
                detalle.set("precio_unitario", pack.getPrecioTotalConDescuento());
                detalle.set("subtotal", pack.getPrecioTotalConDescuento()
                        .multiply(new BigDecimal(pedidoPack.getInteger("cantidad"))));
                detalle.saveIt();
                total = total.add(pack.getPrecioTotalConDescuento()
                        .multiply(new BigDecimal(pedidoPack.getInteger("cantidad"))));
            }
        }

        // Agregar detalle para el envío, si aplica
        if (costoEnvio != null && costoEnvio.compareTo(BigDecimal.ZERO) > 0) {
            PedidoDetalle envioDetalle = new PedidoDetalle();
            envioDetalle.set("pedido_id", this.getId());
            envioDetalle.set("descripcion", "Envío");
            envioDetalle.set("cantidad", 1); // Siempre 1 para servicios como envío
            envioDetalle.set("precio_unitario", costoEnvio);
            envioDetalle.set("subtotal", costoEnvio);
            envioDetalle.saveIt();
            total = total.add(costoEnvio);
        }

        // Agregar detalle para la comisión de Mercado Pago, si aplica
        if (comision != null && comision.compareTo(BigDecimal.ZERO) > 0) {
            PedidoDetalle comisionDetalle = new PedidoDetalle();
            BigDecimal com = total.multiply(comision.divide(new BigDecimal(100)));
            comisionDetalle.set("pedido_id", this.getId());
            comisionDetalle.set("descripcion", "Comisión Mercado Pago");
            comisionDetalle.set("cantidad", 1); // Siempre 1 para servicios como comisión
            comisionDetalle.set("precio_unitario", com);
            comisionDetalle.set("subtotal", com);
            comisionDetalle.saveIt();
            total = total.add(com);
        }
        this.set("total", total);
    }

}

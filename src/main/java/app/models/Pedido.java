package app.models;

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

        // // Agregar productos
        // JSONArray productosArray = new JSONArray();
        // List<Producto> productos = getAll(Producto.class);
        // productos.forEach(producto -> productosArray.put(producto.toJson()));
        // jsonObject.put("productos", productosArray);

        // // Agregar packs
        // JSONArray packsArray = new JSONArray();
        // List<Pack> packs = getAll(Pack.class);
        // packs.forEach(pack -> packsArray.put(pack.toJson()));
        // jsonObject.put("packs", packsArray);

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
}

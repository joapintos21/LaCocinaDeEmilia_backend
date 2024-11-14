package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.List;

@Table("aplicacion.Pack")
public class Pack extends Model {

    static {
        validatePresenceOf("precio");
        validateNumericalityOf("precio").greaterThan(0);
    }

    // Formato JSON para Pack
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", get("id"));
        jsonObject.put("precio", get("precio"));

        // Obtener productos asociados
        JSONArray productosArray = new JSONArray();
        List<Producto> productos = getProductos();
        productos.forEach(producto -> productosArray.put(producto.toJson()));
        jsonObject.put("productos", productosArray);

        return jsonObject;
    }

    // Método para obtener los productos asociados al pack
    public List<Producto> getProductos() {
        return Producto.findBySQL("SELECT p.* FROM aplicacion.Producto p " +
                "JOIN aplicacion.Pack_Producto pp ON p.id = pp.producto_id " +
                "WHERE pp.pack_id = ?", getId());
    }

    // Método para agregar un producto al pack
    public void addProducto(Producto producto) {
        if (producto != null) {
            PackProducto packProducto = new PackProducto();
            packProducto.set("pack_id", this.getId());
            packProducto.set("producto_id", producto.getId());
            packProducto.saveIt();
        }
    }

    // Método para eliminar un producto del pack
    public void removeProducto(Producto producto) {
        if (producto != null) {
            PackProducto packProducto = PackProducto.findFirst("pack_id = ? AND producto_id = ?", this.getId(),
                    producto.getId());
            if (packProducto != null) {
                packProducto.delete();
            }
        }
    }
}
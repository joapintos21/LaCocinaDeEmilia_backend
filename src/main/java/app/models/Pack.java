package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.HasMany;
import org.javalite.activejdbc.annotations.Table;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.List;

@Table("aplicacion.Pack")
@HasMany(child = PackProducto.class, foreignKeyName = "pack_id")
public class Pack extends Model {

    static {
        validatePresenceOf("nombre", "precio");
        validateNumericalityOf("precio").greaterThan(0);
    }

    // Formato JSON para Pack
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", get("id"));
        json.put("nombre", get("nombre"));
        json.put("precio", get("precio"));

        // Obtener los productos asociados al Pack
        List<PackProducto> packProductos = PackProducto.where("pack_id = ?", getId());
        JSONArray productosArray = new JSONArray();

        for (PackProducto packProducto : packProductos) {
            Producto producto = Producto.findById(packProducto.get("producto_id"));
            if (producto != null) {
                JSONObject productoJson = producto.toJson();
                productoJson.put("cantidad", packProducto.get("cantidad")); // Incluye la cantidad del Pack_Producto
                productosArray.put(productoJson);
            }
        }

        json.put("productos", productosArray);
        return json;
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
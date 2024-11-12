package app.models;

import java.util.List;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.HasMany;
import org.javalite.activejdbc.annotations.Table;
import org.json.JSONObject;

@Table("aplicacion.Categoria")
@HasMany(child = ProductoCategoria.class, foreignKeyName = "categoria_id")
public class Categoria extends Model {

    static {
        validatePresenceOf("titulo");
    }

    // Formato JSON para CategoriaProducto
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", get("id"));
        jsonObject.put("titulo", get("titulo"));
        jsonObject.put("descripcion", get("descripcion"));
        return jsonObject;
    }

    // Método para asociar productos
    public void addProducto(Producto producto) {
        if (producto != null) {
            producto.addCategoria(this);
        }
    }

    // Método para eliminar un producto
    public void removeProducto(Producto producto) {
        if (producto != null) {
            producto.removeCategoria(this);
        }
    }

    public List<Producto> getProductos() {
        return Producto.findBySQL("SELECT p.* FROM aplicacion.Producto p " +
                "JOIN aplicacion.Producto_Categoria pc ON p.id = pc.producto_id " +
                "WHERE pc.categoria_id = ?", getId());
    }
}

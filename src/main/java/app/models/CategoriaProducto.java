package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.json.JSONObject;

@Table("aplicacion.CategoriaProducto")
public class CategoriaProducto extends Model {

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
}

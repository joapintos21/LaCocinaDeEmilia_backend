package app.models;

import java.util.List;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.HasMany;
import org.javalite.activejdbc.annotations.Table;
import org.json.JSONObject;

@Table("aplicacion.Categoria")
@HasMany(child = Producto.class, foreignKeyName = "categoria_id")
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

    // Método para obtener todos los productos asociados a esta categoría
    public List<Producto> getProductos() {
        return getAll(Producto.class);
    }
}

package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.HasMany;
import org.javalite.activejdbc.annotations.Table;
import org.json.JSONObject;

@Table("aplicacion.Producto")
@HasMany(child = ProductoAdicional.class, foreignKeyName = "producto_id")
@HasMany(child = PedidoProducto.class, foreignKeyName = "producto_id")
public class Producto extends Model {

    static {
        validatePresenceOf("descripcion", "precio", "categoria_id");
        validateNumericalityOf("precio").greaterThan(0);
    }

    // Formato JSON para Producto
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", get("id"));
        jsonObject.put("descripcion", get("descripcion"));
        jsonObject.put("precio", get("precio"));
        jsonObject.put("visible", get("visible"));
        // Obtener la categoría asociada
        Categoria categoria = Categoria.findById(get("categoria_id"));
        if (categoria != null) {
            jsonObject.put("categoria", categoria.get("titulo")); // Agregar el título de la categoría
        } else {
            jsonObject.put("categoria", JSONObject.NULL); // Manejar el caso de categoría nula
        }

        return jsonObject;
    }
}

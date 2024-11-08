package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.json.JSONObject;

@Table("aplicacion.Producto")
public class Producto extends Model {

    static {
        validatePresenceOf("descripcion", "precio");
        validateNumericalityOf("precio").greaterThan(0);
    }

    // Formato JSON para Producto
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", get("id"));
        jsonObject.put("descripcion", get("descripcion"));
        jsonObject.put("precio", get("precio"));
        jsonObject.put("visible", get("visible"));
        return jsonObject;
    }

    // Método para asociar categorías
    public void addCategoria(CategoriaProducto categoria) {
        if (categoria != null) {
            categoria.add(this);
        }
    }

    // Método para eliminar una categoría
    public void removeCategoria(CategoriaProducto categoria) {
        if (categoria != null) {
            categoria.remove(this);
        }
    }
}

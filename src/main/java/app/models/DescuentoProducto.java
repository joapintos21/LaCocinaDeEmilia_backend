package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.json.JSONObject;

@Table("aplicacion.Descuento_Producto")
public class DescuentoProducto extends Model {
    static {
        validatePresenceOf("producto_id", "descuento_id");
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", get("id")); // ID del DescuentoProducto

        // Agregar detalles del Descuento asociado
        Descuento descuento = Descuento.findById(get("descuento_id"));
        if (descuento != null) {
            jsonObject.put("descuento", descuento.toJson()); // Usa el método toJson de Descuento
        }

        return jsonObject;
    }
}
package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.json.JSONObject;

@Table("aplicacion.producto_adicional")
public class AdicionalProducto extends Model {

    static {
        validatePresenceOf("producto_id", "adicional_id");
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", get("id"));
        jsonObject.put("producto_id", get("producto_id"));
        jsonObject.put("adicional_id", get("adicional_id"));

        // Agregar detalles del descuento asociado
        Adicional adicional = Adicional.findById(get("adicional_id"));
        if (adicional != null) {
            jsonObject.put("adicional", adicional.toJson());
        } else {
            jsonObject.put("adicional", JSONObject.NULL);
        }

        return jsonObject;
    }

    public Long getAdicionalId() {
        return getLong("adicional_id");
    }

    public Long getProductoId() {
        return getLong("producto_id");
    }
}

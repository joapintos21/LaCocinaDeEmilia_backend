package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.json.JSONObject;

@Table("aplicacion.Descuento_Pack")
public class DescuentoPack extends Model {
    static {
        validatePresenceOf("pack_id", "descuento_id");
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", get("id")); // ID de la relación DescuentoPack
        // Agregar detalles del Descuento asociado
        Descuento descuento = Descuento.findById(get("descuento_id"));
        if (descuento != null) {
            jsonObject.put("descuento", descuento.toJson());
        }
        return jsonObject;
    }
}
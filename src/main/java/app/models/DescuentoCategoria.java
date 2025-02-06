package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.json.JSONObject;

@Table("aplicacion.Descuento_Categoria")
public class DescuentoCategoria extends Model {
    static {
        validatePresenceOf("categoria_id", "descuento_id");
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", get("id"));
        jsonObject.put("categoria_id", get("categoria_id"));
        jsonObject.put("descuento_id", get("descuento_id"));

        // Agregar detalles del descuento asociado
        Descuento descuento = Descuento.findById(get("descuento_id"));
        if (descuento != null) {
            jsonObject.put("descuento", descuento.toJson());
        } else {
            jsonObject.put("descuento", JSONObject.NULL);
        }

        return jsonObject;
    }

    public Long getDescuentoId() {
        return getLong("descuento_id");
    }

    public Long getCategoriaId() {
        return getLong("categoria_id");
    }
}
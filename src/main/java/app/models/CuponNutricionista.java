package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.json.JSONObject;

@Table("gestion_usuarios.CuponNutricionista")
public class CuponNutricionista extends Model {
    static {
        validatePresenceOf("descuento_id", "codigo", "nutricionista_id");
    }

    // Formato JSON para Producto
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", get("id"));
        jsonObject.put("codigo", get("codigo"));
        jsonObject.put("nutricionista_id", get("nutricionista_id"));
        jsonObject.put("referencia", "cupon");
        // Obtener la categoría asociada
        Descuento descuento = Descuento.findById(get("descuento_id"));
        if (descuento != null) {
            jsonObject.put("descuento", descuento.toJson());
        } else {
            jsonObject.put("descuento", JSONObject.NULL);
        }

        return jsonObject;
    }
}
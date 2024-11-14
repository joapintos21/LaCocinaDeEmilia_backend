package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.HasMany;
import org.javalite.activejdbc.annotations.Table;
import org.json.JSONObject;

@Table("aplicacion.Adicional")
@HasMany(child = ProductoAdicional.class, foreignKeyName = "adicional_id")
public class Adicional extends Model {

    static {
        validatePresenceOf("descripcion", "costoAdicional");
        validateNumericalityOf("costoAdicional").allowNull(true).greaterThan(0);
    }

    // Formato JSON para Adicional
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", get("id"));
        jsonObject.put("descripcion", get("descripcion"));
        jsonObject.put("costoAdicional", get("costoAdicional"));
        jsonObject.put("visible", get("visible"));
        return jsonObject;
    }
}
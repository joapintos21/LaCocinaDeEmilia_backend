package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.json.JSONObject;

@Table("aplicacion.Direccion")
public class Direccion extends Model {

    static {
        validatePresenceOf("cliente_id", "titulo", "direccion");
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", get("id"));
        jsonObject.put("cliente_id", get("cliente_id"));
        jsonObject.put("titulo", get("titulo"));
        jsonObject.put("direccion", get("direccion"));
        jsonObject.put("latitud", get("latitud"));
        jsonObject.put("longitud", get("longitud"));
        jsonObject.put("predeterminado", get("predeterminado"));
        return jsonObject;
    }
}
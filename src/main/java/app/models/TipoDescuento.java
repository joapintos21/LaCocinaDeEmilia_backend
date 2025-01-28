package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.json.JSONObject;

@Table("aplicacion.TipoDescuento")
public class TipoDescuento extends Model {

    static {
        validatePresenceOf("nombre");
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", getId());
        json.put("nombre", get("nombre"));
        return json;
    }
}

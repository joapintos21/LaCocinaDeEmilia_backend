package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.HasMany;
import org.javalite.activejdbc.annotations.Table;
import org.json.JSONObject;

@Table("aplicacion.Empresa")
@HasMany(child = Permiso.class, foreignKeyName = "rol_id")
public class Empresa extends Model {

    static {
        validatePresenceOf("nombre");
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", getId());
        json.put("nombre", get("nombre"));
        json.put("direccion", get("direccion"));
        json.put("descripcion_transferencia", get("descripcion_transferencia"));
        json.put("telefono", get("telefono"));
        json.put("correo", get("correo"));
        return json;
    }
}

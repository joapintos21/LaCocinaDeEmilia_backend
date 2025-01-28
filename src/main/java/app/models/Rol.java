package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.HasMany;
import org.javalite.activejdbc.annotations.Table;
import org.json.JSONObject;

@Table("gestion_usuarios.Rol")
@HasMany(child = Permiso.class, foreignKeyName = "rol_id")
public class Rol extends Model {

    // Aquí puedes definir validaciones específicas, si es necesario
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

package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.javalite.activejdbc.annotations.HasMany;

@Table("aplicacion.MetodoPago")
@HasMany(child = Pago.class, foreignKeyName = "metodo_pago_id") // Relación con Pago
public class MetodoPago extends Model {

    static {
        // Validaciones
        validatePresenceOf("nombre", "comision")
                .message("Faltan datos obligatorios para el método de pago");
    }

    // Método para convertir un MetodoPago a JSON
    public String toJson() {
        return new org.json.JSONObject()
                .put("id", getId())
                .put("nombre", get("nombre"))
                .put("comision", get("comision"))
                .toString();
    }
}
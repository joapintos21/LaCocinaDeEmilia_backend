package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.BelongsTo;
import org.javalite.activejdbc.annotations.Table;

@Table("aplicacion.Envio")
@BelongsTo(parent = Pedido.class, foreignKeyName = "pedido_id")
public class Envio extends Model {
    static {
        validatePresenceOf("pedido_id", "direccion_id", "costo");
    }

    /**
     * Convert the Envio instance to a JSON object.
     *
     * @return A JSON representation of the Envio instance.
     */
    public org.json.JSONObject toJson() {
        org.json.JSONObject json = new org.json.JSONObject();
        json.put("id", getId());
        json.put("pedido_id", get("pedido_id"));
        json.put("direccion_id", get("direccion_id"));
        json.put("fechaEnvio", getDate("fechaEnvio"));
        json.put("costo", getBigDecimal("costo"));
        json.put("notasCadete", get("notasCadete"));
        json.put("entregado", getBoolean("entregado"));
        return json;
    }
}
package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.CompositePK;
import org.javalite.activejdbc.annotations.Table;
import org.json.JSONObject;

@Table("aplicacion.Pedido_Pack")
@CompositePK({ "pedido_id", "pack_id" })
public class PedidoPack extends Model {
    static {
        validatePresenceOf("pedido_id", "pack_id", "cantidad");
        validateNumericalityOf("cantidad").greaterThan(0);
    }

    // Método para serializar a JSON
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("pedido_id", get("pedido_id"));
        json.put("pack_id", get("pack_id"));
        json.put("cantidad", get("cantidad"));

        // Opcional: agregar detalles del Pack si están cargados
        Pack pack = parent(Pack.class);
        if (pack != null) {
            json.put("pack", pack.toJson());
        }
        return json;
    }
}
package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.javalite.activejdbc.annotations.BelongsTo;

@Table("aplicacion.Pago")
@BelongsTo(parent = Pedido.class, foreignKeyName = "pedido_id") // Relación con Pedido
public class Pago extends Model {

    static {
        // Validaciones
        validatePresenceOf("pedido_id", "metodo_pago_id", "estado_pago", "monto")
                .message("Faltan datos obligatorios para registrar el pago");
        validateNumericalityOf("monto").greaterThan(0).message("El monto debe ser mayor a 0");
    }

    // Método para convertir un Pago a JSON
    public String toJson() {
        return new org.json.JSONObject()
                .put("id", getId())
                .put("pedido_id", get("pedido_id"))
                .put("metodo_pago_id", get("metodo_pago_id"))
                .put("estado_pago", get("estado_pago"))
                .put("monto", get("monto"))
                .put("referencia", get("referencia"))
                .put("fecha_pago", get("fecha_pago"))
                .put("observaciones", get("observaciones"))
                .toString();
    }
}
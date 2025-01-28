package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.BelongsTo;
import org.javalite.activejdbc.annotations.Table;

@Table("aplicacion.PedidoDetalle")
@BelongsTo(parent = Pedido.class, foreignKeyName = "pedido_id")
public class PedidoDetalle extends Model {

    static {
        validatePresenceOf("pedido_id", "descripcion", "precio_unitario", "subtotal");
        validateNumericalityOf("cantidad").greaterThan(0);
    }

    /**
     * Método para convertir el detalle del pedido a JSON.
     * 
     * @return Objeto JSON con los detalles del pedido.
     */
    public String toJson() {
        return toMap().toString(); // Convierte los datos del modelo en un mapa y luego a JSON.
    }
}
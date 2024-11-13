package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("aplicacion.Pedido_Pack")
public class PedidoPack extends Model {

    static {
        validatePresenceOf("pedido_id", "pack_id", "cantidad");
        validateNumericalityOf("cantidad").greaterThan(0);
    }
}
package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.CompositePK;
import org.javalite.activejdbc.annotations.Table;

@Table("aplicacion.Pedido_Producto")
@CompositePK({ "pedido_id", "producto_id" })
public class PedidoProducto extends Model {

    static {
        validatePresenceOf("pedido_id", "producto_id", "cantidad");
        validateNumericalityOf("cantidad").greaterThan(0);
    }
}
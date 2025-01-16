package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.CompositePK;
import org.javalite.activejdbc.annotations.Table;

@Table("aplicacion.Pedido_Producto_Adicional")
@CompositePK({ "pedido_producto_id", "adicional_id" })
public class PedidoProductoAdicional extends Model {

    static {
        validatePresenceOf("pedido_producto_id", "adicional_id");
    }
}
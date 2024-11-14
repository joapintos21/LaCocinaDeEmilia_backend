package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("aplicacion.Pedido_Producto_Adicional")
public class PedidoProductoAdicional extends Model {

    static {
        validatePresenceOf("pedido_id", "producto_id", "adicional_id");
    }
}
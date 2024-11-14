package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("aplicacion.Pack_Producto")
public class PackProducto extends Model {

    static {
        validatePresenceOf("pack_id", "producto_id");
    }
}
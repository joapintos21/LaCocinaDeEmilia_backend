package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.CompositePK;
import org.javalite.activejdbc.annotations.Table;

@Table("aplicacion.Pack_Producto")
@CompositePK({ "pack_id", "producto_id" })
public class PackProducto extends Model {

    static {
        validatePresenceOf("pack_id", "producto_id", "cantidad");
        validateNumericalityOf("cantidad").greaterThan(0);
    }
}
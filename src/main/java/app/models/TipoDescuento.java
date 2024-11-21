package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("aplicacion.TipoDescuento")
public class TipoDescuento extends Model {
    static {
        validatePresenceOf("nombre");
    }
}
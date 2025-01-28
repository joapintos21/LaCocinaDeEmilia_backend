package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("aplicacion.Descuento_Categoria")
public class DescuentoCategoria extends Model {
    static {
        validatePresenceOf("categoria_id", "descuento_id");
    }
}
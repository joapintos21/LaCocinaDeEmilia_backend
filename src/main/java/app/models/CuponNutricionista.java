package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("gestion_usuarios.CuponNutricionista")
public class CuponNutricionista extends Model {
    static {
        validatePresenceOf("descuento_id", "codigo");
    }
}
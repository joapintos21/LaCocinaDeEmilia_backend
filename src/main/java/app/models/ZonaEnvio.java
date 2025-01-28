package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("aplicacion.ZonasEnvio")
public class ZonaEnvio extends Model {
    static {
        validatePresenceOf("nombre", "coordenadas", "costo");
    }
}
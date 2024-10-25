package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("gestion_usuarios.Rol")
public class Rol extends Model {
    // Aquí puedes definir validaciones específicas, si es necesario
    static {
        validatePresenceOf("nombre");
    }
}

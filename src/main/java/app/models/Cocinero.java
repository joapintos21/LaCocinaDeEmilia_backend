package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("gestion_usuarios.Cocinero")
public class Cocinero extends Model {
    static {
        validatePresenceOf("usuario_id");
    }

    public Usuario getUsuario() {
        return Usuario.findById(get("usuario_id"));
    }
}
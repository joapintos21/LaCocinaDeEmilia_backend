package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.HasMany;
import org.javalite.activejdbc.annotations.Table;

@Table("gestion_usuarios.Cliente")
@HasMany(child = Direccion.class, foreignKeyName = "cliente_id")
public class Cliente extends Model {
    static {
        validatePresenceOf("usuario_id");
    }

    public Usuario getUsuario() {
        return Usuario.findById(get("usuario_id"));
    }
}
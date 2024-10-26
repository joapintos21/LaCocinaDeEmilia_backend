package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("gestion_usuarios.Nutricionista")
public class Nutricionista extends Model {
    static {
        validatePresenceOf("cliente_id");
    }

    // Obtener el cliente asociado
    public Cliente getCliente() {
        return Cliente.findById(get("cliente_id"));
    }

    // Obtener el usuario asociado a través de Cliente
    public Usuario getUsuario() {
        Cliente cliente = getCliente();
        return cliente != null ? cliente.getUsuario() : null;
    }
}
package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.json.JSONObject;

@Table("aplicacion.Pedido_Historico")
public class PedidoHistorico extends Model {

    static {
        validatePresenceOf("pedido_id", "estado_nuevo", "fecha_cambio", "usuario_id");
    }

    // Formato JSON para Pedido_Historico
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", get("id"));
        jsonObject.put("pedido_id", get("pedido_id"));
        jsonObject.put("estado_anterior", get("estado_anterior"));
        jsonObject.put("estado_nuevo", get("estado_nuevo"));
        jsonObject.put("fecha_cambio", get("fecha_cambio"));
        jsonObject.put("usuario_id", get("usuario_id"));
        jsonObject.put("observaciones", get("observaciones"));
        return jsonObject;
    }

    public static PedidoHistorico registrarCambio(Pedido pedido, String estadoAnterior, String estadoNuevo,Usuario usuario, String observaciones) {
        PedidoHistorico historico = new PedidoHistorico();
        historico.set("pedido_id", pedido.getId());
        historico.set("estado_anterior", estadoAnterior);
        historico.set("estado_nuevo", estadoNuevo);
        historico.set("usuario_id", usuario.getId());
        historico.set("fecha_cambio", new java.sql.Timestamp(System.currentTimeMillis()));
        historico.set("observaciones", observaciones);

        if (historico.saveIt()) {
            System.out.println("Registro exitoso en PedidoHistorico.");
        } else {
            System.out.println("Errores al guardar en PedidoHistorico: " + historico.errors().toString());
        }

        return historico;
    }
}
package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Table("aplicacion.Gastos")
public class Gasto extends Model {

    @Override
    public Long getId() {
        return getLong("id");
    }

    static {
        validatePresenceOf("descripcion", "monto", "fecha", "fecha_carga");
        validateNumericalityOf("monto").greaterThan(0);
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", get("id"));
        jsonObject.put("descripcion", get("descripcion"));
        jsonObject.put("monto", getBigDecimal("monto").setScale(2, BigDecimal.ROUND_HALF_UP));
        jsonObject.put("fecha", get("fecha").toString());
        jsonObject.put("fecha_carga", get("fecha_carga").toString());

        return jsonObject;
    }
}
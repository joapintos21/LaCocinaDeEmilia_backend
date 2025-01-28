package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.BelongsTo;
import org.javalite.activejdbc.annotations.HasMany;
import org.javalite.activejdbc.annotations.Table;
import org.json.JSONObject;

@Table("aplicacion.Descuento")
@BelongsTo(parent = TipoDescuento.class, foreignKeyName = "tipo_id")
@HasMany(child = DescuentoProducto.class, foreignKeyName = "descuento_id")
@HasMany(child = DescuentoPack.class, foreignKeyName = "descuento_id")
public class Descuento extends Model {
    static {
        validatePresenceOf("tipo_id", "valor", "descripcion");
        validateNumericalityOf("valor").greaterThan(0);
    }

    @Override
    public void beforeSave() {
        if (get("descripcion") == null || get("descripcion") == "") {
            set("descripcion", "Descuento"); // Valor por defecto si no está definido
        }
    }

    public TipoDescuento getTipoDescuento() {
        return parent(TipoDescuento.class);
    }

    public void setTipoDescuento(TipoDescuento tipoDescuento) {
        setParent(tipoDescuento);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", getId());
        json.put("tipo", getTipoDescuento() != null ? getTipoDescuento().getString("nombre") : JSONObject.NULL);
        json.put("valor", get("valor"));
        json.put("fechainicio", get("fechainicio"));
        json.put("fechafin", get("fechafin"));
        json.put("descripcion", get("descripcion")); // Incluye el nuevo campo
        return json;
    }
}
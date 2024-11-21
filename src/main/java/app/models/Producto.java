package app.models;

import java.util.List;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.HasMany;
import org.javalite.activejdbc.annotations.Table;
import org.json.JSONArray;
import org.json.JSONObject;

@Table("aplicacion.Producto")
@HasMany(child = ProductoAdicional.class, foreignKeyName = "producto_id")
@HasMany(child = PedidoProducto.class, foreignKeyName = "producto_id")
@HasMany(child = PackProducto.class, foreignKeyName = "producto_id")
@HasMany(child = DescuentoProducto.class, foreignKeyName = "producto_id")
public class Producto extends Model {

    static {
        validatePresenceOf("descripcion", "precio", "categoria_id");
        validateNumericalityOf("precio").greaterThan(0);
    }

    public DescuentoProducto getDescuento() {
        return this.parent(DescuentoProducto.class);
    }

    // Formato JSON para Producto
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", get("id"));
        jsonObject.put("descripcion", get("descripcion"));
        jsonObject.put("precio", get("precio"));
        jsonObject.put("visible", get("visible"));
        // Obtener la categoría asociada
        Categoria categoria = Categoria.findById(get("categoria_id"));
        if (categoria != null) {
            jsonObject.put("categoria", categoria.get("titulo")); // Agregar el título de la categoría
        } else {
            jsonObject.put("categoria", JSONObject.NULL); // Manejar el caso de categoría nula
        }

        // Obtener los descuentos asociados
        List<DescuentoProducto> descuentos = getAll(DescuentoProducto.class);
        JSONArray descuentosArray = new JSONArray();
        for (DescuentoProducto descuento : descuentos) {
            JSONObject descuentoJson = descuento.toJson(); // Asegúrate de que DescuentoProducto tiene un método
                                                           // toJson()
            descuentosArray.put(descuentoJson);
        }
        jsonObject.put("descuentos", descuentosArray);

        return jsonObject;
    }
}

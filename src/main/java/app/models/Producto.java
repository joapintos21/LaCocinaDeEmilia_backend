package app.models;

import java.util.List;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.HasMany;
import org.javalite.activejdbc.annotations.Table;
import org.json.JSONObject;

@Table("aplicacion.Producto")
@HasMany(child = ProductoAdicional.class, foreignKeyName = "producto_id")
@HasMany(child = PedidoProducto.class, foreignKeyName = "producto_id")
@HasMany(child = ProductoCategoria.class, foreignKeyName = "producto_id")
public class Producto extends Model {

    static {
        validatePresenceOf("descripcion", "precio");
        validateNumericalityOf("precio").greaterThan(0);
    }

    // Formato JSON para Producto
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", get("id"));
        jsonObject.put("descripcion", get("descripcion"));
        jsonObject.put("precio", get("precio"));
        jsonObject.put("visible", get("visible"));
        return jsonObject;
    }

    // Método para asociar categorías
    public void addCategoria(Categoria categoria) {
        if (categoria != null) {
            categoria.add(this);
        }
    }

    // Método para eliminar una categoría
    public void removeCategoria(Categoria categoria) {
        if (categoria != null) {
            categoria.remove(this);
        }
    }

    public List<Categoria> getCategorias() {
        return Categoria.findBySQL("SELECT c.* FROM aplicacion.Categoria c " +
                "JOIN aplicacion.Producto_Categoria pc ON c.id = pc.categoria_id " +
                "WHERE pc.producto_id = ?", getId());
    }
}

package app.models;

import java.math.BigDecimal;
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

    @Override
    public Long getId() {
        return getLong("id");
    }

    static {
        validatePresenceOf("descripcion", "precio", "categoria_id");
        validateNumericalityOf("precio").greaterThan(0);
    }

    public DescuentoProducto getDescuento() {
        return DescuentoProducto.findFirst("producto_id = ?", this.getId());
    }

    public BigDecimal getPrecioTotalConDescuento() {
        BigDecimal precioBase = this.getBigDecimal("precio");
        BigDecimal totalDescuento = BigDecimal.ZERO;

        DescuentoProducto descuentoProducto = this.getDescuento();
        if (descuentoProducto != null) {
            Descuento descuento = Descuento.findById(descuentoProducto.get("descuento_id"));
            int tipo = descuento.getInteger("tipo_id");
            TipoDescuento tipoDescuento = TipoDescuento.findFirst("id = ?", tipo);
            BigDecimal valor = descuento.getBigDecimal("valor");

            if ("PORCENTAJE".equalsIgnoreCase(tipoDescuento.getString("nombre"))) {
                totalDescuento = totalDescuento.add(precioBase.multiply(valor));
            } else if ("IMPORTE".equalsIgnoreCase(tipoDescuento.getString("nombre"))) {
                totalDescuento = totalDescuento.add(valor);
            }
        }

        BigDecimal precioFinal = precioBase.subtract(totalDescuento);
        return precioFinal.compareTo(BigDecimal.ZERO) > 0 ? precioFinal : BigDecimal.ZERO;
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
            jsonObject.put("categoria", categoria.toJson()); // Agregar el título de la categoría
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

        // Usamos una consulta con JOIN para obtener los adicionales relacionados
        List<Adicional> adicionales = Adicional.findBySQL(
                "SELECT a.* FROM aplicacion.Adicional a " +
                        "JOIN aplicacion.Producto_Adicional pa ON a.id = pa.adicional_id " +
                        "WHERE pa.producto_id = ?",
                this.getId());

        JSONArray adicionalesJson = new JSONArray();
        adicionales.forEach(adicional -> adicionalesJson.put(adicional.toJson()));
        jsonObject.put("adicionales", adicionalesJson);

        return jsonObject;
    }

    public JSONObject toDescuentoJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", get("id"));
        jsonObject.put("descripcion", get("descripcion"));
        jsonObject.put("precio", get("precio"));
        jsonObject.put("visible", get("visible"));

        return jsonObject;
    }
}

package app.models;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.BelongsTo;
import org.javalite.activejdbc.annotations.HasMany;
import org.javalite.activejdbc.annotations.Table;
import org.json.JSONObject;

@Table("aplicacion.Direccion")
@BelongsTo(parent = Cliente.class, foreignKeyName = "cliente_id")
@HasMany(child = Envio.class, foreignKeyName = "direccion_id")
public class Direccion extends Model {

    static {
        validatePresenceOf("cliente_id", "titulo", "direccion");
    }

    public double calcularCostoEnvio() {
        Direccion direccion = Direccion.findById(this.getId());
        if (direccion == null) {
            return -1;
        }
        double latitud = direccion.getDouble("latitud");
        double longitud = direccion.getDouble("longitud");

        // Aquí llamas al método que verifica los polígonos y calcula el costo
        double costo = buscarCostoEnvio(latitud, longitud);
        return costo;
    }

    private double buscarCostoEnvio(double latitud, double longitud) {
        String query = "SELECT costo " +
                "FROM aplicacion.zonasenvio " +
                "WHERE ST_Contains(ST_GeomFromGeoJSON(coordenadas), ST_SetSRID(ST_Point(?, ?), 4326))";

        try (PreparedStatement ps = Base.connection().prepareStatement(query)) {
            ps.setDouble(1, longitud);
            ps.setDouble(2, latitud);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("costo");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al calcular el costo de envío: " + e.getMessage());
        }

        // Retornar un costo predeterminado si no se encuentra una zona que contenga el
        // punto
        return -1;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", get("id"));
        jsonObject.put("cliente_id", get("cliente_id"));
        jsonObject.put("titulo", get("titulo"));
        jsonObject.put("direccion", get("direccion"));
        jsonObject.put("latitud", get("latitud"));
        jsonObject.put("longitud", get("longitud"));
        jsonObject.put("predeterminado", get("predeterminado"));
        return jsonObject;
    }

    // Validate if the logged-in user is the owner of the current pedido
    public boolean isOwner(int userId) {
        Cliente c = parent(Cliente.class);
        return c.getInteger("id") == userId;
    }
}
package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.json.JSONObject;

@Table("gestion_usuarios.Permisos")
public class Permiso extends Model {
    public static boolean tienePermiso(int rolId, String endpoint, String tipo) {
        String baseEndpoint = endpoint.split("/")[1]; // Obtiene la primera parte del path
        // Buscar permiso con coincidencia exacta
        Permiso permiso = findFirst("rol_id = ? AND endpoint = ? AND tipo = ?", rolId, baseEndpoint, tipo);

        if (permiso != null) {
            return true; // Coincidencia exacta
        }

        // Si el tipo es LECTURA, verifica si existe un permiso con tipo ESCRITURA
        if ("LECTURA".equals(tipo)) {
            permiso = findFirst("rol_id = ? AND endpoint = ? AND tipo = ?", rolId, baseEndpoint, "ESCRITURA");
            return permiso != null;
        }
        return false;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", getId());
        json.put("rol_id", get("rol_id"));
        json.put("endpoint", get("endpoint"));
        json.put("tipo", get("tipo"));
        return json;
    }
}
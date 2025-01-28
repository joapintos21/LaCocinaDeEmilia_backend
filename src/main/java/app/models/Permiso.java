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

    public static String formatText(String texto) {
        if (texto == null || texto.isEmpty()) {
            return "";
        }
        // Convertir la primera letra a mayúscula y el resto a minúsculas
        return texto.substring(0, 1).toUpperCase() + texto.substring(1).toLowerCase();
    }

    public JSONObject toBeautifyJson() {
        JSONObject json = new JSONObject();
        json.put("id", getId());
        json.put("rol", Rol.findById(get("rol_id")).get("nombre"));
        json.put("rol_id", get("rol_id"));
        json.put("endpoint", formatText(get("endpoint").toString()));
        json.put("tipo", formatText(get("tipo").toString()));
        return json;
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
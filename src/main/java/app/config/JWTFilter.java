package app.config;

import app.models.Permiso;
import app.models.Usuario;
import java.util.Set;
import org.javalite.activeweb.AppContext;
import org.javalite.activeweb.controller_filters.HttpSupportFilter;

public class JWTFilter extends HttpSupportFilter {

    private static final Set<String> EXCLUDED_PATHS = Set.of(
            "/hooks/handleWebhook",
            "/usuarios/create",
            "/usuarios/validarToken",
            "/login/iniciarSesion",
            "instagram/obtenerMedia");

    private static final Set<String> EXCLUDED_CONTROLLER_PATHS = Set.of(
            "/productos",
            "/categorias",
            "/packs");

    @Override
    public void before() {
        // Excluye paths específicos de EXCLUDED_PATHS
        if (EXCLUDED_PATHS.stream().anyMatch(path()::endsWith)) {
            return;
        }
        if (method().equalsIgnoreCase("GET") && EXCLUDED_CONTROLLER_PATHS.stream().anyMatch(path()::startsWith)) {
            return;
        }
        // Obtener el token desde el encabezado Authorization
        String token = header("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            // Renderiza un mensaje JSON y establece el código de estado en 401
            respond("{\"error\": \"Token no proporcionado o inválido.\"}").contentType("application/json").status(401);
            return;
        }
        // Eliminar "Bearer " para obtener el token
        token = token.substring(7);
        // Llamar al método verifyJwt para validar el token y obtener el usuario
        Usuario usuario = Usuario.verifyJwt(token);
        if (usuario == null) {
            // Token inválido o expirado
            respond("{\"error\": \"Token inválido o expirado.\"}").contentType("application/json").status(401);
            return;
        }

        // Almacenar userId en el contexto
        AppContext context = appContext();
        context.set("userId", usuario.getId());
        context.set("rolId", usuario.getInteger("rol_id"));

        // Obtener el método y el endpoint
        String metodo = method(); // GET, POST, PUT, DELETE
        String endpoint = path(); // Obtiene el endpoint actual

        // Determinar el tipo de permiso necesario
        String tipoPermiso = metodo.equals("GET") ? "LECTURA" : "ESCRITURA";

        // Obtener el rol del usuario desde el contexto
        int rolId = (Integer) context.get("rolId");

        // Verificar permisos
        if (!Permiso.tienePermiso(rolId, endpoint, tipoPermiso)) {
            respond("{\"error\": \"No tienes permiso para acceder a este recurso.\"}")
                    .contentType("application/json")
                    .status(403); // Forbidden
            return;
        }
    }

    @Override
    public void after() {
        // Lógica después de la acción del controlador, si es necesario
    }
}
package app.config;

import app.models.Usuario;
import java.util.Set;
import org.javalite.activeweb.controller_filters.HttpSupportFilter;

public class JWTFilter extends HttpSupportFilter {

    private static final Set<String> EXCLUDED_PATHS = Set.of(
            "/usuarios/create",
            "/usuarios/validarToken",
            "/login/iniciarSesion");

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
        appContext().set("userId", usuario.getId()); // Almacenar userId en el contexto
    }

    @Override
    public void after() {
        // Lógica después de la acción del controlador, si es necesario
    }
}
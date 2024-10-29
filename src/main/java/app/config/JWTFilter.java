package app.config;

import app.models.Usuario;
import org.javalite.activeweb.controller_filters.HttpSupportFilter;

public class JWTFilter extends HttpSupportFilter {

    @Override
    public void before() {
        // Verifica si el método es el que deseas omitir
        if (path().endsWith("/usuarios/create")) {
            return;
        }
        // Obtener el token desde el encabezado Authorization
        String token = header("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            // Renderiza un mensaje JSON y establece el código de estado en 401
            respond("{\"error\": \"Token no proporcionado o inválido.\"}").status(401);
            return;
        }
        // Eliminar "Bearer " para obtener el token
        token = token.substring(7);
        // Llamar al método verifyJwt para validar el token y obtener el usuario
        Usuario usuario = Usuario.verifyJwt(token);
        if (usuario == null) {
            // Token inválido o expirado
            respond("{\"error\": \"Token inválido o expirado.\"}").status(401);
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
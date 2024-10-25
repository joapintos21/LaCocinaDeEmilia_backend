package app.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.javalite.activeweb.controller_filters.HttpSupportFilter;

public class JWTFilter extends HttpSupportFilter {

    // Clave secreta para firmar el token
    private static final String SECRET_KEY = "your-secret-key";

    @Override
    public void before() {
        // Obtener el token desde el encabezado Authorization
        String token = header("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            // Renderiza un mensaje JSON y establece el código de estado en 401
            respond("{\"error\": \"Token no proporcionado o inválido.\"}").status(401);
            return;
        }

        // Eliminar "Bearer " para obtener el token
        token = token.substring(7);

        try {
            // Verificar el token JWT
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);

            // Extraer el userId del token y almacenarlo en el contexto
            String userId = jwt.getSubject();
            appContext().set("userId", userId); // Almacenar userId en el contexto

        } catch (JWTVerificationException e) {
            // En caso de error en la verificación, renderizar el error
            respond("{\"error\": \"Token inválido o expirado.\"}").status(401);
        }
    }

    @Override
    public void after() {
        // Lógica después de la acción del controlador, si es necesario
    }
}
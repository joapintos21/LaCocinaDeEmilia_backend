package app.controllers;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.javalite.activejdbc.Base;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.DELETE;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.PUT;
import org.json.JSONArray;
import org.json.JSONObject;

import app.models.Rol;
import app.models.Usuario;
import app.services.EmailService;
import io.github.cdimascio.dotenv.Dotenv;

public class UsuariosController extends AppController {

    private static final Logger logger = LogManager.getLogger(UsuariosController.class);
    private static final Dotenv dotenv = Dotenv.load();

    // Crear un nuevo usuario con un rol
    @POST
    public void create(Usuario user) {
        try {
            // Establecer la contraseña del usuario (en caso de que no se haya hecho)
            user.setPassword(user.getString("password"));

            // Establecer la fecha de creación del usuario con la fecha actual del sistema
            user.setFechaCreacion(); // Esto asignará la fecha actual al campo "fecha_creacion" del usuario

            // Intentar guardar el usuario
            if (user.save()) {
                // Obtener el rol del usuario (si existe)
                Integer rolId = user.getInteger("rol_id");
                if (rolId != null && rolId != 0) {
                    user.assignRol(rolId); // Asignar el rol al usuario
                }

                // Generar token de validación de cuenta
                String token = user.generateJwtValidation();
                String baseUrl = dotenv.get("VALIDATION_LINK_BASE_URL");
                String validationLink = baseUrl + "/verificar?token=" + token;

                // Enviar el email de validación
                EmailService emailService = new EmailService();
                String contentText = String.format(
                        "<p>Hola %s,</p><p>Gracias por registrarte en nuestra aplicación. "
                                + "Por favor, confirma tu cuenta haciendo clic en el siguiente enlace:</p>"
                                + "<a href='%s'>Confirma tu cuenta</a><p>¡Gracias!</p>",
                        user.get("nombre"), validationLink);

                emailService.sendTestEmail(
                        user.getString("email"),
                        "Validación de cuenta",
                        contentText);

                // Responder con los datos del usuario
                logger.info(user.toString());
                respond(user.formatoJSON().toString()).contentType("application/json").status(200);
            } else {
                // En caso de error, devolver los errores de validación
                logger.error(user.errors().toString());
                String errorJson = "{\"error\": " + user.errors().toJSON() + "}";
                respond(errorJson).contentType("application/json").status(400);
            }
        } catch (Exception e) {
            // Capturar y devolver el error específico si ocurre
            String errorMessage = e.getMessage();

            // Opcionalmente, puedes personalizar el mensaje de error para situaciones
            // específicas
            if (errorMessage.contains("usuario_email_key")) {
                errorMessage = "El correo electrónico ya está registrado.";
            }
            if (errorMessage.contains("usuario_telefono_key")) {
                errorMessage = "El teléfono ya está registrado.";
            }
            logger.error(errorMessage);
            String errorJson = "{\"error\": \"" + errorMessage + "\"}";
            respond(errorJson).contentType("application/json").status(422);
        }
    }

    // Actualizar un usuario y su rol
    @PUT
    public void update(Usuario newUser) {
        int id = Integer.parseInt(param("id"));
        Usuario usuario = Usuario.findById(id);

        if (usuario != null) {
            try {
                Base.openTransaction();
                // Guarda el rol actual
                // Copia los atributos de newUser en usuario sin rol_id
                Map<String, Object> userAttributes = newUser.toMap();
                userAttributes.remove("rol_id"); // Remueve rol_id para evitar cambios
                usuario.fromMap(userAttributes);

                // Si el campo "password" en newUser no es nulo ni vacío, lo actualizamos
                String password = newUser.getString("password");
                if (password != null && !password.isEmpty()) {
                    usuario.setPassword(password);
                }

                if (usuario.saveIt()) {
                    logger.info(usuario.toString());
                    Base.commitTransaction();
                    respond(usuario.formatoJSON().toString()).contentType("application/json").status(200);
                } else {
                    logger.error(usuario.errors().toString());
                    Base.rollbackTransaction();
                    System.out.println(usuario.errors());
                    respond(usuario.errors().toString()).contentType("application/json").status(400);
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
                Base.rollbackTransaction();
                System.out.println(e.getMessage());
                respond("Error al actualizar el usuario").contentType("application/json").status(500);
            }
        } else {
            logger.error("Usuario no encontrado. ON UPDATE");
            respond("Usuario no encontrado").contentType("application/json").status(404);
        }
    }

    // Obtener todos los usuarios
    @GET
    public void index() {
        List<Usuario> usuarios = Usuario.findAll();
        JSONArray jsonArray = new JSONArray();

        // Crear un JSONObject solo con los atributos deseados
        usuarios.forEach(usuario -> {
            jsonArray.put(usuario.formatoJSON());
        });
        String json = jsonArray.toString();
        logger.info(json);
        respond(json).contentType("application/json");
    }

    // Obtener todos los tipos de roles de usuarios
    @GET
    public void roles() {
        List<Rol> roles = Rol.findAll();
        JSONArray jsonArray = new JSONArray();
        roles.forEach(rol -> jsonArray.put(rol.toJson()));
        logger.info(jsonArray.toString());
        respond(jsonArray.toString()).contentType("application/json");
    }

    // Obtener un usuario por ID
    @GET
    public void show() {
        int id = Integer.parseInt(param("id"));
        Usuario usuario = Usuario.findById(id);

        if (usuario != null) {
            logger.info(usuario.toString());
            respond(usuario.formatoJSON().toString()).contentType("application/json").status(200);
        } else {
            logger.error("Usuario no encontrado. ON GET");
            respond("Usuario no encontrado").contentType("application/json").status(404);
        }
    }

    @DELETE
    public void delete() {
        int id = Integer.parseInt(param("id"));
        Usuario usuario = Usuario.findById(id);
        if (usuario != null) {
            logger.info("Usuario eliminado correctamente: " + usuario.toString());
            usuario.delete(); // Eliminación del usuario (la eliminación en cascada se maneja en la base de
            // datos)
            respond("Usuario eliminado correctamente").contentType("application/json").status(200);
        } else {
            logger.error("Usuario no encontrado. ON DELETE");
            respond("Usuario no encontrado").contentType("application/json").status(404);
        }
    }

    @GET
    public void validarToken() {
        String token = param("token");

        // Llamar al método verifyJwt para validar el token y obtener el usuario
        Usuario user = Usuario.verifyJwt(token);
        if (user == null) {
            // Token inválido o expirado
            respond("{\"error\": \"Token inválido o expirado.\"}").contentType("application/json").status(401);
            return;
        }

        if (user != null && "pendiente".equals(user.get("status"))) {
            // Actualizar el estado del usuario a activo
            user.set("status", "activo");
            user.save();
            logger.info("Cuenta verificada exitosamente: " + user.toString());
            respond("{\"mensaje\": \"Cuenta verificada exitosamente\"}")
                    .contentType("application/json")
                    .status(200);
        } else {
            logger.error("Usuario no encontrado o ya verificado. ON ValidarToken");
            respond("{\"error\": \"Usuario no encontrado o ya verificado\"}")
                    .contentType("application/json")
                    .status(400);
        }
    }

    // Endpoint para solicitar recuperación de contraseña
    @POST
    public void forgotPassword() {
        JSONObject body = new JSONObject(getRequestString());
        String email = body.getString("email");

        Usuario user = Usuario.findFirst("email = ?", email);
        if (user == null) {
            logger.error("No se encontró un usuario con ese correo. ON forgotPassword");
            respond("{\"error\": \"No se encontró un usuario con ese correo.\"}")
                    .contentType("application/json")
                    .status(404);
            return;
        }

        // Generar token de recuperación
        String token = user.generateJwtValidation();
        user.set("reset_token", token);
        user.saveIt();

        // Enviar correo con enlace de recuperación
        String resetLink = "http://localhost:3000/reset-password?token=" + token;
        String emailContent = String.format(
                "<p>Hola %s,</p><p>Haz clic en el siguiente enlace para restablecer tu contraseña:</p>"
                        + "<a href='%s'>Restablecer Contraseña</a><p>Este enlace es válido por un tiempo limitado.</p>",
                user.get("nombre"), resetLink);

        EmailService emailService = new EmailService();
        emailService.sendTestEmail(email, "Recuperación de Contraseña", emailContent);
        logger.info("Se ha enviado un correo con instrucciones para recuperar la contraseña para el usuario: "
                + user.toString());
        respond("{\"message\": \"Se ha enviado un correo con instrucciones para recuperar la contraseña.\"}")
                .contentType("application/json")
                .status(200);
    }

    // Endpoint para resetear la contraseña
    @POST
    public void resetPassword() {
        JSONObject body = new JSONObject(getRequestString());
        String token = body.getString("token");
        String newPassword = body.getString("password");

        Usuario user = Usuario.findFirst("reset_token = ?", token);
        if (user != null) {
            user = Usuario.verifyJwt(token);
        }
        if (user == null) {
            logger.error("Token inválido o expirado. ON resetPassword");
            respond("{\"error\": \"Token inválido o expirado.\"}")
                    .contentType("application/json")
                    .status(400);
            return;
        }

        // Actualizar contraseña
        user.setPassword(newPassword);
        user.set("reset_token", null); // Eliminar token usado
        user.saveIt();

        logger.info("Contraseña restablecida correctamente para el usuario: " + user.toString());
        respond("{\"message\": \"Contraseña restablecida correctamente.\"}")
                .contentType("application/json")
                .status(200);
    }
}

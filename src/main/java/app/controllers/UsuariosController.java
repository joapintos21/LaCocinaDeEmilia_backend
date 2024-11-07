package app.controllers;

import app.models.Usuario;
import app.services.EmailService;
import java.util.List;
import java.util.Map;
import org.javalite.activejdbc.Base;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.DELETE;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.PUT;
import org.json.JSONArray;

public class UsuariosController extends AppController {

    // Crear un nuevo usuario con un rol
    @POST
    public void create(Usuario user) {
        try {
            user.setPassword(user.getString("password"));

            if (user.save()) {
                Integer rolId = user.getInteger("rol_id"); // Suponiendo que el rol viene con el usuario
                if (rolId != null && rolId != 0) {
                    user.assignRol(rolId); // Asigna el rol al usuario
                }
                String token = user.generateJwtValidation();
                String validationLink = "http://localhost:3000/verificar?token=" + token;

                EmailService emailService = new EmailService();
                // Contenido del email en HTML
                String contentText = String.format(
                        "<p>Hola %s,</p><p>Gracias por registrarte en nuestra aplicación. " +
                                "Por favor, confirma tu cuenta haciendo clic en el siguiente enlace:</p>" +
                                "<a href='%s'>Confirma tu cuenta</a><p>¡Gracias!</p>",
                        user.get("nombre"), validationLink);

                emailService.sendTestEmail(
                        user.getString("email"),
                        "Validación de cuenta",
                        contentText);

                respond(user.formatoJSON().toString()).contentType("application/json").status(200);
            } else {
                // Construir una respuesta JSON con los errores de validación
                String errorJson = "{\"error\": " + user.errors().toJSON() + "}";
                respond(errorJson).contentType("application/json").status(400); // Devuelve un código de estado 400 para
                                                                                // indicar error
            }
        } catch (Exception e) {
            // Capturar la excepción y devolver un mensaje de error JSON
            String errorMessage = e.getMessage();

            // Opcionalmente, puedes filtrar el mensaje para obtener detalles específicos
            if (errorMessage.contains("usuario_email_key")) {
                errorMessage = "El correo electrónico ya está registrado.";
            }
            if (errorMessage.contains("usuario_telefono_key")) {
                errorMessage = "El teléfono ya está registrado.";
            }

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
                    Base.commitTransaction();
                    respond(usuario.formatoJSON().toString()).contentType("application/json").status(200);
                } else {
                    Base.rollbackTransaction();
                    System.out.println(usuario.errors());
                    respond(usuario.errors().toString()).contentType("application/json").status(400);
                }
            } catch (Exception e) {
                Base.rollbackTransaction();
                System.out.println(e.getMessage());
                respond("Error al actualizar el usuario").contentType("application/json").status(500);
            }
        } else {
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
        respond(json).contentType("application/json");
    }

    // Obtener un usuario por ID
    @GET
    public void show() {
        int id = Integer.parseInt(param("id"));
        Usuario usuario = Usuario.findById(id);

        if (usuario != null) {
            respond(usuario.formatoJSON().toString()).contentType("application/json").status(200);
        } else {
            respond("Usuario no encontrado").contentType("application/json").status(404);
        }
    }

    @DELETE
    public void delete() {
        int id = Integer.parseInt(param("id"));
        Usuario usuario = Usuario.findById(id);
        if (usuario != null) {
            usuario.delete(); // Eliminación del usuario (la eliminación en cascada se maneja en la base de
                              // datos)
            respond("Usuario eliminado correctamente").status(200);
        } else {
            respond("Usuario no encontrado").status(404);
        }
    }

    @GET
    public void validarToken() {
        String token = param("token");

        // Llamar al método verifyJwt para validar el token y obtener el usuario
        Usuario user = Usuario.verifyJwt(token);
        if (user == null) {
            // Token inválido o expirado
            respond("{\"error\": \"Token inválido o expirado.\"}").status(401);
            return;
        }

        if (user != null && "pendiente".equals(user.get("status"))) {
            // Actualizar el estado del usuario a activo
            user.set("status", "activo");
            user.save();

            respond("{\"mensaje\": \"Cuenta verificada exitosamente\"}")
                    .contentType("application/json")
                    .status(200);
        } else {
            respond("{\"error\": \"Usuario no encontrado o ya verificado\"}")
                    .contentType("application/json")
                    .status(400);
        }
    }
}
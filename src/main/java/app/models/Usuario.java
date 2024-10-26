package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.mindrot.jbcrypt.BCrypt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Base64;
import java.util.Date;

@Table("gestion_usuarios.Usuario")
public class Usuario extends Model {
    private static final String SECRET_KEY = Base64.getEncoder().encodeToString("secret_key_jwt_token".getBytes());
    private static final long EXPIRATION_TIME = 86400000; // 1 día en milisegundos

    static {
        validatePresenceOf("email", "password", "nombre", "telefono", "rol_id");
    }

    public void assignRol(Integer rolId) {
        Rol rol = Rol.findById(rolId);
        set("rol_id", rol.getId());
        saveIt();

        switch (rolId) {
            case 4: // Rol ID 4 es "Cliente"
                ensureClienteRecord();
                break;
            case 5: // Rol ID 5 es "Nutricionista"
                ensureNutricionistaRecord();
                break;
            // Puedes agregar otros roles en futuros casos aquí
            default:
                // Opcional: Manejar casos en que el rol no requiera acción específica
                break;
        }
    }

    // Método para crear el registro en la tabla Cliente si no existe
    private void ensureClienteRecord() {
        if (Cliente.where("usuario_id = ?", this.getId()).isEmpty()) {
            Cliente cliente = new Cliente();
            cliente.set("usuario_id", this.getId());
            cliente.saveIt();
        }
    }

    // Método para crear el registro en la tabla Nutricionista si no existe
    private void ensureNutricionistaRecord() {
        // Asegurar que el usuario también es cliente
        ensureClienteRecord();
        Cliente cliente = Cliente.findFirst("usuario_id = ?", this.getId());

        if (Nutricionista.where("cliente_id = ?", cliente.getId()).isEmpty()) {
            Nutricionista nutricionista = new Nutricionista();
            nutricionista.set("cliente_id", cliente.getId());
            nutricionista.set("saldo", 0.0);
            nutricionista.saveIt();
        }
    }

    // Hash de contraseña
    public void setPassword(String password) {
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        set("password", hashed);
    }

    // Verificar contraseña
    public boolean checkPassword(String password) {
        return BCrypt.checkpw(password, getString("password"));
    }

    // Generar JWT
    public String generateJwt() {
        return Jwts.builder()
                .setSubject(getString("id"))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    // Verificar JWT
    public static Usuario verifyJwt(String token) {
        try {
            String userIdStr = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();

            // Convertir userId a Integer
            Integer userId = Integer.parseInt(userIdStr);
            return Usuario.findById(userId);
        } catch (NumberFormatException e) {
            System.out.println("El userId no es un número válido: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("Error de verificación del token: " + e.getMessage());
            return null; // Token inválido o expirado
        }
    }
}
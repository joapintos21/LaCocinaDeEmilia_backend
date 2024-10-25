package app.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.mindrot.jbcrypt.BCrypt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

@Table("gestion_usuarios.Usuario")
public class Usuario extends Model {
    private static final String SECRET_KEY = "your_super_secret_key"; // Usar una clave segura en producción
    private static final long EXPIRATION_TIME = 86400000; // 1 día en milisegundos

    static {
        validatePresenceOf("email", "password", "nombre", "telefono", "rol_id");
    }

    public void assignRol(Integer rolId) {
        Rol rol = Rol.findById(rolId);
        set("rol_id", rol.getId());
        saveIt();

        if (rolId == 4) {
            ensureClienteRecord();
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
            String userId = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();

            return Usuario.findById(userId);
        } catch (Exception e) {
            return null; // Token inválido o expirado
        }
    }
}
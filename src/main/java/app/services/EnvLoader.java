package app.services;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvLoader {
    public static void load() {
        Dotenv dotenv = Dotenv.configure()
                .directory("/Users/pedrobonilla/Personal/ORT/LaCocinaDeEmilia_backend")
                .load();

        // Cargar variables en el entorno
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });

        System.out.println("DB_PASS: " + dotenv.get("DB_PASS")); // Para probar si se carga
    }
}
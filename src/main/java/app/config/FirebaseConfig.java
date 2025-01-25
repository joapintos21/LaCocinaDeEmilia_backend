package app.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.FileInputStream;
import java.io.IOException;

public class FirebaseConfig {
    private static boolean isInitialized = false; // 🔹 Variable para evitar múltiples inicializaciones

    public static void initializeFirebase() throws IOException {
        if (!isInitialized && FirebaseApp.getApps().isEmpty()) {
            FileInputStream serviceAccount = new FileInputStream("src/main/resources/firebase-adminsdk.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket("stellar-fx-444302-f2.firebasestorage.app") // 🔹 Cambia el dominio incorrecto
                    .build();

            FirebaseApp.initializeApp(options);
            isInitialized = true; // 🔥 Marcamos Firebase como inicializado
            System.out.println("✅ Firebase inicializado correctamente.");
        }
    }
}
package app.services;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import java.util.Base64;
import java.util.UUID;

public class FirebaseStorageService {

    // public static String subirArchivo(String base64, String contentType, String
    // folder) throws Exception {
    // byte[] archivoBytes = Base64.getDecoder().decode(base64);
    // String nombreArchivo = folder + "/" + UUID.randomUUID().toString(); // 🔹
    // Genera un nombre único

    // Bucket bucket = StorageClient.getInstance().bucket();
    // Blob blob = bucket.create(nombreArchivo, archivoBytes, contentType);

    // // 🔥 Obtener la URL pública de Firebase desde `Blob`
    // return blob.getMediaLink(); // 📌 URL pública generada automáticamente por
    // Firebase
    // }

    public static String subirPDF(String pdfBase64) throws Exception {
        byte[] pdfBytes = Base64.getDecoder().decode(pdfBase64);
        String nombreArchivo = "recetas/" + UUID.randomUUID().toString() + ".pdf"; // 🔥 Ruta en Firebase

        Bucket bucket = StorageClient.getInstance().bucket();
        Blob blob = bucket.create(nombreArchivo, pdfBytes, "application/pdf");

        return "https://firebasestorage.googleapis.com/v0/b/" + bucket.getName() + "/o/"
                + nombreArchivo.replace("/", "%2F") + "?alt=media"; // 🔹 URL pública del PDF
    }

    public static void eliminarArchivo(String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.isEmpty()) {
                return;
            }

            // 🔹 Obtener el bucket
            Bucket bucket = StorageClient.getInstance().bucket();

            // 🔥 Extraer el filePath correcto desde la URL
            String filePath = imageUrl.substring(imageUrl.indexOf("/o/") + 3); // 🔹 Elimina "/o/"
            filePath = filePath.split("\\?")[0]; // 🔹 Elimina parámetros de la URL
            filePath = filePath.replace("%2F", "/"); // 🔹 Reemplaza "%2F" por "/"

            // 🔥 Intentar obtener el archivo en Firebase Storage
            Blob blob = bucket.get(filePath);

            if (blob != null) {
                blob.delete();
                System.out.println("✅ Imagen eliminada de Firebase: " + filePath);
            } else {
                System.out.println("⚠ No se encontró la imagen en Firebase. Verifica el nombre: " + filePath);
            }
        } catch (Exception e) {
            System.err.println("❌ Error eliminando la imagen de Firebase: " + e.getMessage());
        }
    }
}
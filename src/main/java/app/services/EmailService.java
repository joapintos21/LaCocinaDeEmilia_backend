package app.services;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.TrackingSettings;
import com.sendgrid.helpers.mail.objects.ClickTrackingSetting;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import java.io.IOException;

public class EmailService {

    private static final String SENDGRID_API_KEY = "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";
    private static final String FROM_EMAIL = "gestion@lacocinadeemilia.com.uy"; // Cambia esto por tu email

    public void sendTestEmail(String toEmail, String subject, String contentText) {
        Email from = new Email(FROM_EMAIL);
        Email to = new Email(toEmail);
        Content content = new Content("text/html", contentText);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(SENDGRID_API_KEY);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            System.out.println("Status Code: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody());
            System.out.println("Headers: " + response.getHeaders());

        } catch (IOException ex) {
            System.err.println("Error enviando el correo: " + ex.getMessage());
        }
    }

    public static void enviarCorreoConPDF(String destinatario, String pdfUrl) throws IOException {
        Email from = new Email(FROM_EMAIL);
        Email to = new Email(destinatario);
        String subject = "Tu Recetario Digital";
        Content content = new Content("text/html",
                "<p>Gracias por tu compra.</p>" +
                        "<p>Puedes descargar tu recetario aquí: <a href='" + pdfUrl + "'>Descargar PDF</a></p>");

        Mail mail = new Mail(from, subject, to, content);

        // Deshabilitar el rastreo de clics
        ClickTrackingSetting clickTrackingSetting = new ClickTrackingSetting();
        clickTrackingSetting.setEnable(false);
        TrackingSettings trackingSettings = new TrackingSettings();
        trackingSettings.setClickTrackingSetting(clickTrackingSetting);
        mail.setTrackingSettings(trackingSettings);

        SendGrid sg = new SendGrid(SENDGRID_API_KEY);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println("Email enviado: " + response.getStatusCode());
        } catch (IOException ex) {
            throw new IOException("Error enviando email: " + ex.getMessage());
        }
    }
}
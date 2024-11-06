package app.services;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import java.io.IOException;

public class EmailService {

    private static final String SENDGRID_API_KEY = "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";

    public void sendTestEmail(String toEmail, String subject, String contentText) {
        Email from = new Email("pbonilla091@gmail.com"); // Remitente temporal
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
}
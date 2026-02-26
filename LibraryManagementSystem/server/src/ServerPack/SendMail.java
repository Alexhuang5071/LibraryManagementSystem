/*
 * EE422C Final Project submission by
 * Replace <... ...> with your actual data.
 * <Alex Huang>
 * <ah57984>
 * <17185>
 * Spring 2023
 */
package ServerPack;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import java.io.IOException;

public class SendMail {
    public static synchronized void sendEmail(String toEmail, String subject, String contentText) {
        Email from = new Email("alexhuang5071@utexas.edu");
        Email to = new Email(toEmail);
        Content content = new Content("text/plain", contentText);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(("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"));
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        } catch (IOException ex) {
            System.out.println("Error Sending Mail: " + ex.getMessage());
        }
    }
}

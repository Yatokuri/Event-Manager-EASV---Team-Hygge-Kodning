    package gui.util;

    import gui.model.DisplayErrorModel;
    import java.io.File;
    import java.io.FileInputStream;
    import java.io.IOException;
    import java.util.Properties;
    import java.util.concurrent.*;
    import javax.mail.*;
    import javax.mail.internet.*;

    public class MailSender {
        private static final String configFile = "config/config.settings";

        public static void sendEmailWithAttachments(String toAddress, String subject, String message, File attachFiles) throws IOException {
            Properties mailProperties = new Properties();
            mailProperties.load(new FileInputStream((configFile)));

            final String username = mailProperties.getProperty("EmailName").trim();
            final String password = mailProperties.getProperty("EmailPassword").trim();

            // sets SMTP server properties
            Properties properties = getProperties();

            // creates a new session with an authenticator
            Authenticator auth = new Authenticator() {
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            };

            Session session = Session.getInstance(properties, auth);
            //session.setDebug(true);  // Enable debugging

            // creates a new e-mail message
            Message msg = new MimeMessage(session);

            try {
                msg.setFrom(new InternetAddress(username));
                InternetAddress[] toAddresses = {new InternetAddress(toAddress)};
                msg.setRecipients(Message.RecipientType.TO, toAddresses);
                msg.setSubject(subject);
                msg.setSentDate(new java.util.Date());

                // creates message part
                MimeBodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setContent(message, "text/plain; charset=UTF-8");
                messageBodyPart.setHeader("Content-Type", "text/plain; charset=UTF-8");
                // creates multi-part
                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(messageBodyPart);
                MimeBodyPart attachPart = new MimeBodyPart();
                attachPart.attachFile(attachFiles);
                multipart.addBodyPart(attachPart);

                // sets the multi-part as e-mail's content
                msg.setContent(multipart);

                // sends the e-mail
                Transport.send(msg);
            } catch (MessagingException e) {
                new DisplayErrorModel().displayErrorC("Could not send the email");
            }
        }

        public static boolean testConnection() {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Boolean> future = executor.submit(() -> {
                try {

                    Properties mailProperties = new Properties();
                    mailProperties.load(new FileInputStream((configFile)));

                    final String username = mailProperties.getProperty("EmailName").trim();
                    final String password = mailProperties.getProperty("EmailPassword").trim();

                    Properties properties = getProperties();

                    Authenticator auth = new Authenticator() {
                        public PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    };

                    Session session = Session.getInstance(properties, auth);

                    // Attempt to connect to the SMTP server
                    Transport transport = session.getTransport("smtp");
                    transport.connect(username, password);
                    transport.close();
                    return true;
                } catch (Exception e) {
                    // Log or handle the exception as needed
                    return false;
                }
            });

            try {
                // Wait for the future result with a timeout of 2 seconds
                return future.get(1, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                return false;
            } finally {
                executor.shutdownNow();
            }
        }

        private static Properties getProperties() {
            Properties properties = new Properties();
            properties.put("mail.smtp.host", "smtp.gmail.com");
            properties.put("mail.smtp.port", "587");
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            return properties;
        }
    }

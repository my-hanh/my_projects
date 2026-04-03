package com.zhaw.it.pm3.tournamentgenerator.services;

import com.zhaw.it.pm3.tournamentgenerator.persons.Player;
import com.zhaw.it.pm3.tournamentgenerator.persons.Team;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.imageio.ImageIO;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * The {@code MessageCaster} class is responsible for sending emails and capturing the tournament tree as an image.
 * <p>
 * It allows sending emails with an attached tournament tree image to all players in specified teams.
 * The tournament tree is captured from a JavaFX {@link Pane}.
 */
public class MessageCaster {

    private static String senderEmail;

    private static String senderPassword;

    /**
     * Constructs a {@code MessageCaster} with the specified sender email and password.
     *
     * @param senderEmail    the email address of the sender.
     * @param senderPassword the password of the sender's email account.
     */
    public MessageCaster(String senderEmail, String senderPassword) {
        this.senderEmail = senderEmail;
        this.senderPassword = senderPassword;
    }

    /**
     * Sends an email with an attached tournament tree image to all players in the specified teams.
     *
     * @param pane      the {@link Pane} containing the tournament tree to capture.
     * @param FilePath  the file path where the tournament tree image is saved.
     * @param teams     the list of {@link Team} objects containing players to whom emails will be sent.
     * @throws MessagingException if an error occurs while sending the email.
     * @throws IOException        if an error occurs while saving the tournament tree image.
     */
    public void sendEmailToAllPlayers(Pane pane, String FilePath, ArrayList<Team> teams) throws MessagingException, IOException {
        captureTournamentTree(pane);
        for (Team team : teams) {
            for (Player person : team.getPlayers()) {
                if (person.getEmail() != null && !person.getEmail().isEmpty()) {
                    sendEmail(person.getEmail(), FilePath);
                }
            }
        }
    }

    /**
     * Sends an email with an attached file to the specified recipient.
     *
     * @param recipientEmail the email address of the recipient.
     * @param filePath       the file path of the attachment.
     * @throws MessagingException if an error occurs while sending the email.
     * @throws IOException        if an error occurs while accessing the file attachment.
     */
    public static void sendEmail(String recipientEmail, String filePath) throws MessagingException, IOException {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        //setup email message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderEmail));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
        message.setSubject("Tournament Tree");

        //create a multipart message
        Multipart multipart = new MimeMultipart();

        //create a message body part
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText("Please find the tournament tree attached.");
        multipart.addBodyPart(textPart);

        //attach the tournament tree
        MimeBodyPart attachmentPart = new MimeBodyPart();
        attachmentPart.setDataHandler(new DataHandler(new FileDataSource(filePath)));
        attachmentPart.setFileName("tournament_tree.png");
        multipart.addBodyPart(attachmentPart);

        message.setContent(multipart);

        Transport.send(message);
        System.out.println("Sending email to " + recipientEmail);
    }

    /**
     * Captures a snapshot of the specified JavaFX {@link Pane} and saves it as an image file.
     *
     * @param pane the {@link Pane} containing the tournament tree to capture.
     * @throws IOException if an error occurs while saving the image.
     */
    public static void captureTournamentTree(Pane pane) throws IOException {
        Platform.runLater(() -> {

            File file = new File("tournament_tree.png");
            System.out.println("Saving image to: " + file.getAbsolutePath());

            WritableImage image = pane.snapshot(new SnapshotParameters(), null);
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            } catch (IOException e) {
                e.printStackTrace();

            }
        });
    }


}

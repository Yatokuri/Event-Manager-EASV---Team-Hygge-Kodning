package gui.util;

import be.TicketSold;
import be.Tickets;
import gui.model.DisplayErrorModel;
import gui.model.ImageModel;
import gui.model.TicketModel;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TicketToPDF {

    private static final int EVENT_TICKET_WIDTH = 900;
    private static final int EVENT_TICKET_HEIGHT = 250;
    private static final int ONE_TIME_TICKET_WIDTH = 200;
    private static final int ONE_TIME_TICKET_HEIGHT = 300;

    private final DisplayErrorModel displayErrorModel;
    private boolean isLocal;

    private final ImageModel systemIMGModel;
    private final TicketModel ticketModel;
    private static TicketSold currentSoldTicket;
    private static Tickets currentTicket;
    private static be.Event currentSoldTicketEvent;
    private static boolean shouldWeSendEmail = false;

    @FXML
    private Pane ticketArea;
    List<GridPane> pageGrids = new ArrayList<>();

    HashMap<String, Image> imgList = new HashMap<>();

    public TicketToPDF() throws Exception {
        displayErrorModel = new DisplayErrorModel();
        systemIMGModel = ImageModel.getInstance();
        ticketModel = TicketModel.getInstance();
    }

    public static void printTickets(GridPane pageGrid, int pageNumber) throws Exception {
        File outputFile;
        // Create a PDF document
        try (PDDocument doc = new PDDocument()) {
            // Create a new page
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            // Create a PDPageContentStream for drawing
            try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                // Convert JavaFX GridPane snapshot to a WritableImage
                WritableImage writableImage = pageGrid.snapshot(null, null);
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
                // Convert BufferedImage to PDImageXObject
                PDImageXObject pdImage = LosslessFactory.createFromImage(doc, bufferedImage);
                // Draw the image onto the PDF page
                contentStream.drawImage(pdImage, 0, 0, PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight());
            }
            // Save the PDF document
            outputFile = getOutputFile(pageNumber);
            doc.save(outputFile);
        }



        if (shouldWeSendEmail) { // Prepare the subject and message
            String subject = "Here's your ticket!";
            String message = "Dear " + currentSoldTicket.getFirstName() + ",\n\n"
                    + "We are thrilled to inform you that your ticket purchase for our upcoming event has been successfully processed!\n\n"
                    + "Event Details:" + "\n";
            if (!currentSoldTicketEvent.getEventName().isEmpty())  {
                message += "Event Name: " + currentSoldTicketEvent.getEventName() + "\n";
            }
            if (!currentSoldTicketEvent.getEventStartDateTime().isEmpty())  {
                message += "Date: " + timeDateConverter(currentSoldTicketEvent.getEventStartDateTime());
            }
            if (!currentSoldTicketEvent.getEventEndDateTime().isEmpty())  {
                message += " - " + timeDateConverter(currentSoldTicketEvent.getEventEndDateTime()) + "\n";
            }
            else {
                message += "\n";
            }
            message += """

                    Please find attached your ticket(s) for the event. Make sure to keep them safe and handy for entry.

                    We look forward to seeing you at the event and hope you have a fantastic time!

                    Best regards,
                    EASV""";

            MailSender.sendEmailWithAttachments(currentSoldTicket.getEmail(), subject, message, getOutputFile(1));

            if (outputFile.exists()) {
               outputFile.delete();
            }

        }
    }

    public static String timeDateConverter(String timeDate)   { // Format LocalDateTime object to desired format
        LocalDateTime dateTime = LocalDateTime.parse(timeDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));
        return dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }


    private static File getOutputFile(int pageNumber) {
        String fileName;
        if (shouldWeSendEmail) {
            fileName = currentSoldTicket.getFirstName() + " " + currentSoldTicket.getLastName() + " " + currentTicket.getTicketName() + ".pdf";
        } else {
            fileName = "Ticket_Page_" + pageNumber + ".pdf";
        }
        Path downloadFolder = Paths.get(System.getProperty("user.home"), "Downloads");
        File outputFile = new File(downloadFolder.toFile(), fileName);
        if (!outputFile.getParentFile().exists()) {
            // If the Downloads folder doesn't exist, save the file in the project folder
            outputFile = new File(fileName);
        }
        return outputFile;
    }
    public void makeGlobalTicketToPDF(Tickets currentTicket, int result, Pane ticketArea, Boolean shouldWeSendEmail) throws Exception {
        ticketModel.setCurrentTicket(currentTicket);
        TicketSold NewTicketSold = new TicketSold("Global", "Global" , "Global", currentTicket.getTicketID(), -10);
        List<TicketSold> ticketCopies = new ArrayList<>(Collections.nCopies(result, NewTicketSold));
        makeTicketsToPDF(ticketCopies, ticketArea, shouldWeSendEmail);
    }

    public void makeTicketToPDF(TicketSold ticketSold, Pane ticketArea, be.Event currentEvent, Boolean shouldWeSendEmail) throws Exception {
        currentSoldTicketEvent = currentEvent;
        currentSoldTicket = ticketSold; // So we can print it later
        TicketToPDF.shouldWeSendEmail = shouldWeSendEmail;
        List<TicketSold> singleTicketList = new ArrayList<>();
        singleTicketList.add(ticketSold);
        makeTicketsToPDF(singleTicketList, ticketArea, shouldWeSendEmail);
    }
    public void makeTicketsToPDF(List<TicketSold> ticketSoldList,  Pane ticketArea,Boolean shouldWeSendEmail) throws Exception {
        TicketToPDF.shouldWeSendEmail = shouldWeSendEmail;
        this.ticketArea = ticketArea;
        currentTicket = ticketModel.getCurrentTicket();
        pageGrids.clear();
        ticketArea.getChildren().clear();
        int ticketsPerPage = 0; // Assuming 9 tickets per page
        int pageNumber = 0;
        int rows = 0; // Number of rows per page
        int cols = 0; // Number of columns per page
        if (currentTicket.getIsILocal() == 1){
            ticketsPerPage = 4;
            pageNumber = 1;
            rows = 4;
            cols = 1;
            isLocal = true;
        }
        else if (currentTicket.getIsILocal() == 0){
            ticketsPerPage = 16;
            pageNumber = 1;
            rows = 4;
            cols = 4;
            isLocal = false;
        }
        for (int i = 0; i < ticketSoldList.size(); i += ticketsPerPage) {
            GridPane pageGrid = new GridPane();   // Create a new page GridPane to store tickets
            pageGrid.setHgap(10);
            pageGrid.setVgap(10);
            pageGrid.setPadding(new Insets(10));

            // Calculate the number of tickets to display on this page
            int remainingTickets = ticketSoldList.size() - i;
            int ticketsToDisplay = Math.min(remainingTickets, ticketsPerPage);

            // Add ticket panes to the page GridPane
            for (int j = 0; j < ticketsPerPage; j++) {
                Pane ticketPane;
                if (j < ticketsToDisplay) {
                    Tickets ticket = ticketModel.readTicket(ticketSoldList.get(j).getTicketID());
                    ticketPane = generateTicket(ticket, ticketSoldList.get(j)); // Generate ticket for the current ID
                } else {
                    ticketPane = new Pane(); // Otherwise, create an empty placeholder
                    ticketPane.setPrefSize(ONE_TIME_TICKET_WIDTH, ONE_TIME_TICKET_HEIGHT); // Set size to match ticket panes
                }
                int row = (j / cols) % rows; // Calculate the row index (0 to 2)
                int col = j % cols; // Calculate the column index (0 to 2)
                pageGrid.add(ticketPane, col, row); // Add to the GridPane
            }

            ticketArea.getChildren().add(pageGrid); // Add GridPane to the scene
            printTickets(pageGrid, pageNumber);

            pageGrids.add(pageGrid);
            pageNumber++;
        }
    }
    private final Map<Integer, String> ticketJSONCache = new HashMap<>(); // Cache to store ticket JSON, so we don't want to get the JSOn if its same ticket type
    private Pane generateTicket(Tickets ticket, TicketSold ticketSold) throws Exception {
        int ticketID = ticket.getTicketID();
        String ticketJSON;
        // Check if the ticket JSON is already cached, so it can go faster
        if (ticketJSONCache.containsKey(ticketID)) {
            ticketJSON = ticketJSONCache.get(ticketID);
        } else { // If not we get the JSOn from db
            ticketJSON = ticket.getTicketJSON();
            ticketJSONCache.put(ticketID, ticketJSON);
        }
        Pane ticketPane = setupTicketView(ticketJSON, ticketArea, ticketSold);

        if (ticketModel.readTicket(ticketID).getIsILocal() == 1)
            ticketPane.setPrefSize(EVENT_TICKET_WIDTH, EVENT_TICKET_HEIGHT);
        if (ticketModel.readTicket(ticketID).getIsILocal() == 0)
            ticketPane.setPrefSize(ONE_TIME_TICKET_WIDTH, ONE_TIME_TICKET_HEIGHT);
        ticketPane.setStyle("-fx-background-color: #cbecec");
        return ticketPane;
    }

    public Pane setupTicketView(String json, Pane paneName, TicketSold ticketsold) throws Exception {
        paneName.getChildren().clear(); // We clear the area, then we recreate it from the JSON
        Pane recreatedPane = TicketSerializerRecreate.cloneTicketAreaFromJson(json);
        // When its recreate we inset right Image from database
        for (Node node : recreatedPane.getChildren()) {
            if (node instanceof ImageView imageView) {
                String id = imageView.getId();
                Image image = getImageByID(id);
                if (imageView.getProperties().containsKey("isQRCode")) { //If there is a QR code we generate a new one with the user unique code
                    String uniqueCode;
                    if (ticketsold.getTransactionID() == -10)   {
                        uniqueCode = ticketModel.generateNewGlobalTicketCode(ticketsold);
                    }
                    else {
                        uniqueCode = ticketModel.readNewSoldTicketCode(ticketsold);
                    }
                    assert image != null;
                    BufferedImage qrCodeImage = BarCode.generateQRCodeImage(uniqueCode, (int) image.getWidth(), (int) image.getHeight());
                    Image QRfxImage = SwingFXUtils.toFXImage(qrCodeImage, null);
                    imageView.setImage(QRfxImage); //We convert QR to real IMG
                }
                else if (imageView.getProperties().containsKey("isBarcode")) {
                    UUID uuid = UUID.randomUUID();
                    assert image != null;
                    BufferedImage barcodeImage = BarCode.generateCode128BarcodeImage(uuid, (int) image.getWidth(), (int) image.getHeight());
                    Image BarcodefxImage = SwingFXUtils.toFXImage(barcodeImage, null);
                    imageView.setImage(BarcodefxImage);
                }
                else if (image != null) {
                    imageView.setImage(image);
                }
            }
            if (node instanceof Label lbl) { // Here auto generated user info on ticket
                if (lbl.getProperties().containsKey("isAFName")) {
                    lbl.setText(ticketsold.getFirstName());
                }
                if (lbl.getProperties().containsKey("isALName")) {
                    lbl.setText(ticketsold.getLastName());
                }
                if (lbl.getProperties().containsKey("isAFLName")) {
                    lbl.setText(ticketsold.getFirstName() + " " + ticketsold.getLastName());
                }
                if (isLocal) {
                    lbl.setMaxWidth(EVENT_TICKET_WIDTH);
                } else {
                    lbl.setMaxWidth(ONE_TIME_TICKET_HEIGHT);
                }
            }
        }
        return recreatedPane;
    }

    private Image getImageByID(String imageViewId) {
        if (imgList.get(imageViewId) != null)   {
            return imgList.get(imageViewId); // Cache IMG so we don't load the same again
        }
        try {
            Image img = systemIMGModel.readSystemIMG(Integer.parseInt(imageViewId)).getImage();
            imgList.put(imageViewId, img);
            return img;
        } catch (Exception e) {
            displayErrorModel.displayErrorC("Could not read image on the ticket");
        }
        return null;
    }
}
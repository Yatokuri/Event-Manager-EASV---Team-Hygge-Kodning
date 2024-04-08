package gui.util;

import be.TicketSold;
import be.Tickets;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import gui.model.DisplayErrorModel;
import gui.model.ImageModel;
import gui.model.TicketModel;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class TicketToPDF {

    private static final int EVENT_TICKET_WIDTH = 940;
    private static final int EVENT_TICKET_HEIGHT = 250;
    private static final int ONE_TIME_TICKET_WIDTH = 250;
    private static final int ONE_TIME_TICKET_HEIGHT = 350;

    private final DisplayErrorModel displayErrorModel;


    private final ImageModel systemIMGModel;
    private final TicketModel ticketModel;


    @FXML
    private Pane ticketArea;
    List<GridPane> pageGrids = new ArrayList<>();

    public TicketToPDF() throws Exception {
        displayErrorModel = new DisplayErrorModel();
        systemIMGModel = ImageModel.getInstance();
        ticketModel = TicketModel.getInstance();
    }

    public static void printTickets(GridPane pageGrid, int pageNumber) throws IOException {
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
            File outputFile = getOutputFile(pageNumber);
            doc.save(outputFile);
        }
        // Close the document
    }

    private static File getOutputFile(int pageNumber) {
        Path downloadFolder = Paths.get(System.getProperty("user.home"), "Downloads");
        File outputFile = new File(downloadFolder.toFile(), "Ticket_Page_" + pageNumber + ".pdf");
        if (!downloadFolder.toFile().exists()) {
            // If the Downloads folder doesn't exist, save the file in the project folder
            outputFile = new File("Ticket_Page_" + pageNumber + ".pdf");
        }
        return outputFile;
    }
    public void makeGlobalTicketToPDF(Tickets currentTicket, int result) throws Exception {
       // ticketModel.readTicket(ticketSold.getTicketID());
        System.out.println(currentTicket + " - " + result);
        List<Tickets> ticketCopies = new ArrayList<>(Collections.nCopies(result, currentTicket));
       // makeTicketsToPDF(ticketCopies, ticketArea);
    }

    public void makeTicketToPDF(TicketSold ticketSold, Pane ticketArea) throws Exception {
        List<TicketSold> singleTicketList = new ArrayList<>();
        singleTicketList.add(ticketSold);
        makeTicketsToPDF(singleTicketList, ticketArea);
    }
    public void makeTicketsToPDF(List<TicketSold> ticketSoldList,  Pane ticketArea) throws Exception {
        this.ticketArea = ticketArea;
        Tickets currentTicket = ticketModel.getCurrentTicket();
        pageGrids.clear();
        // Clear any existing content
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
        }
        else if (currentTicket.getIsILocal() == 0){
            ticketsPerPage = 9;
            pageNumber = 1;
            rows = 3;
            cols = 3;
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

            pageGrids.add(pageGrid);
            pageNumber++;
        }

        /*When all grid pane is created with tickets we set the grid pane in the scene
        where user not can see it and then save and take a snapshot of each we make to PDF page*/
        pageNumber = 1;
        for (GridPane pageGrid : pageGrids) {
            ticketArea.getChildren().add(pageGrid); // Add GridPane to the scene
            final int finalPageNumber = pageNumber;
            Platform.runLater(() -> {
                try {
                    printTickets(pageGrid, finalPageNumber);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
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
                    String uniqueCode = ticketModel.readNewSoldTicketCode(ticketsold);
                    Map<EncodeHintType, ErrorCorrectionLevel> hashmap = new HashMap<>();
                    hashmap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
                    assert image != null;
                    BufferedImage qrCodeImage = BarCode.generateQRCodeImage(uniqueCode, (int) image.getWidth(), (int) image.getHeight());
                    Image QRfxImage = SwingFXUtils.toFXImage(qrCodeImage, null);
                    imageView.setImage(QRfxImage); //We convert QR to real IMG
                }
                else if (image != null) {
                    imageView.setImage(image);
                }
            }
        }
        return recreatedPane;
    }

    private Image getImageByID(String imageViewId) {
        try {
            return systemIMGModel.readSystemIMG(Integer.parseInt(imageViewId)).getImage();
        } catch (Exception e) {
            displayErrorModel.displayErrorC("Could not read image on the ticket");
        }
        return null;
    }
}
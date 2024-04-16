package gui.model;

import be.Tickets;
import bll.GlobalTicketManager;
import bll.TicketManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class GlobalTicketsModel {

    private static volatile GlobalTicketsModel instance;
    private final GlobalTicketManager globalTicketManager;
    private final TicketManager ticketManager;
    private final ObservableList<Tickets> globalTicketsToBeViewed;

    private GlobalTicketsModel() throws Exception {
        globalTicketManager = new GlobalTicketManager();
        ticketManager = new TicketManager();
        globalTicketsToBeViewed = FXCollections.observableArrayList();
        globalTicketsToBeViewed.addAll(globalTicketManager.getAllGlobalTickets());
    }

    public static GlobalTicketsModel getInstance() throws Exception {
        if (instance == null) {
            synchronized (UserModel.class) { if (instance == null) { instance = new GlobalTicketsModel(); } }
        }
        return instance;
    }

    public void addGlobalTickets(Tickets newtickets) throws Exception { // Sends a request to the database to add a tickets to an event
        ticketManager.createNewTicket(newtickets);
        globalTicketManager.addTicketToGlobal(newtickets);
        globalTicketsToBeViewed.add(newtickets); // update list // Adds the new tickets to the event observable list
    }

    public void deleteGlobalTickets (Tickets tickets) throws Exception { // Sends a request to the database to delete a tickets from an event
        globalTicketManager.removeImageFromTickets(tickets); // Remove the IMG from db too
        globalTicketManager.removeTicketFromGlobal(tickets);
        ticketManager.deleteTicket(tickets);
        globalTicketsToBeViewed.clear();
        globalTicketsToBeViewed.addAll(globalTicketManager.getAllGlobalTickets()); // Updates the event observable list with the changes
    }

    public ObservableList<Tickets> getObservableGlobalTickets() {return globalTicketsToBeViewed;} // Returns the event
}

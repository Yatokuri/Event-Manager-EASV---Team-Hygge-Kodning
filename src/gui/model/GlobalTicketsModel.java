package gui.model;

import be.Event;
import be.Tickets;
import bll.EventTicketsManager;
import bll.GlobalTicketManager;
import bll.TicketManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class GlobalTicketsModel {
    private EventModel eventModel;

    private static GlobalTicketsModel instance;
    private final GlobalTicketManager globalTicketManager;
    private final TicketManager ticketManager;
    private final ObservableList<Tickets> globalTicketsToBeViewed;

    private GlobalTicketsModel() throws Exception {
        globalTicketManager = new GlobalTicketManager();
        ticketManager = new TicketManager();
        globalTicketsToBeViewed = FXCollections.observableArrayList();
        eventModel = EventModel.getInstance();
        globalTicketsToBeViewed.addAll(globalTicketManager.getAllGlobalTickets());
    }

    public static GlobalTicketsModel getInstance() throws Exception {
        if (instance == null) {
            synchronized (UserModel.class) { if (instance == null) { instance = new GlobalTicketsModel(); } }
        }
        return instance;
    }

    public boolean addGlobalTickets(Tickets newtickets) throws Exception { // Sends a request to the database to add a tickets to a event
        globalTicketManager.addTicketToGlobal(newtickets);
        globalTicketsToBeViewed.add(newtickets); // update list // Adds the new tickets to the event observable list
        return true;
    }

    public void deleteGlobalTickets (Tickets tickets) throws Exception { // Sends a request to the database to delete a tickets from a event
        globalTicketManager.removeImageFromTickets(tickets); // Remove the IMG from db too
        globalTicketManager.removeTicketFromGlobal(tickets);
        ticketManager.deleteTicket(tickets);
        globalTicketsToBeViewed.clear();
        globalTicketsToBeViewed.addAll(globalTicketManager.getAllGlobalTickets()); // Updates the event observable list with the changes
    }

    public ObservableList<Tickets> getObservableGlobalTickets() {return globalTicketsToBeViewed;} // Returns the event
}

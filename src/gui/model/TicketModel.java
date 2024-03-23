package gui.model;

import be.Ticket;
import be.User;
import bll.TicketManager;
import bll.UserManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TicketModel {

    private final TicketManager ticketManager;
    private ObservableList<User> ticketToBeViewed;
    private static TicketModel instance;

    // Private constructor to prevent more than one TicketModel
    private TicketModel() throws Exception {
        ticketManager = new TicketManager();
        ticketToBeViewed = FXCollections.observableArrayList();
       // ticketToBeViewed.addAll(ticketManager.getAllUsers());
    }

    // Public method to get the singleton instance, so we have control over data
    public static TicketModel getInstance() throws Exception {
        if (instance == null) {
            synchronized (TicketModel.class) {
                if (instance == null) {
                    instance = new TicketModel();
                }
            }
        }
        return instance;
    }


    public Ticket getTicket(Ticket ticketToFetch) throws Exception {
        Ticket ticket;
        ticket = ticketManager.getTicket(ticketToFetch);
        return ticket;
    }

    public Ticket createNewTicket(Ticket newTicket) throws Exception {
        Ticket ticket;
        ticket = ticketManager.createNewTicket(newTicket);
        return ticket;
    }

    public void updateTicket(Ticket updatedTicket) throws Exception {
        ticketManager.updateTicket(updatedTicket);
    }

    public void deleteEvent(Ticket selectedTicket) throws Exception {
        ticketManager.deleteTicket(selectedTicket);
    }

}

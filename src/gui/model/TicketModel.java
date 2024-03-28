package gui.model;

import be.TicketSold;
import be.Tickets;
import be.User;
import bll.TicketManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TicketModel {

    private final TicketManager ticketManager;
    private ObservableList<Tickets> ticketSoldToBeViewed;
    private ObservableList<Tickets> ticketToBeViewed;
    private Tickets currentTicket;
    private static TicketModel instance;

    // Private constructor to prevent more than one TicketModel
    private TicketModel() throws Exception {
        ticketManager = new TicketManager();
        ticketSoldToBeViewed = FXCollections.observableArrayList();
        ticketToBeViewed = FXCollections.observableArrayList();
       // ticketSoldToBeViewed.addAll(ticketManager.getAllUsers());
        ticketToBeViewed.addAll(ticketManager.getAllTickets());
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

    //This is for sold/given Ticket
    public TicketSold getSoldTicket(TicketSold ticketSoldToFetch) throws Exception {
        TicketSold ticketSold;
        ticketSold = ticketManager.getSoldTicket(ticketSoldToFetch);
        return ticketSold;
    }

    public TicketSold createNewSoldTicket(TicketSold newTicketSold) throws Exception {
        TicketSold ticketSold;
        ticketSold = ticketManager.createNewSoldTicket(newTicketSold);
        return ticketSold;
    }

    public void updateSoldTicket(TicketSold updatedTicketSold) throws Exception {
        ticketManager.updateSoldTicket(updatedTicketSold);
    }

    public void deleteEvent(TicketSold selectedTicketSold) throws Exception {
        ticketManager.deleteSoldTicket(selectedTicketSold);
    }

    //This is for Ticket
    public Tickets createNewTicket(Tickets tickets) throws Exception {
        Tickets newUser = ticketManager.createNewTicket(tickets);
        ticketToBeViewed.add(newUser);
        return newUser;
    }
    public void deleteTicket(Tickets tickets) throws Exception {
        ticketManager.deleteTicket(tickets); //TODO When you delete you should also remove all image or?
        ticketToBeViewed.remove(tickets);
    }
    public void readTicket(Tickets tickets) throws Exception{ ticketManager.getTicket(tickets);}
    public void updateTicket(Tickets tickets) throws Exception{ ticketManager.updateTicket(tickets);}

    public Tickets getCurrentTicket() { return currentTicket; }
    public void setCurrentTicket(Tickets tickets) {currentTicket = tickets;}
    public ObservableList<Tickets> getObsTickets() { return ticketToBeViewed; }
}

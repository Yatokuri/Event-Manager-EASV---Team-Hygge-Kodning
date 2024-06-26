package gui.model;

import be.TicketSold;
import be.Tickets;
import bll.TicketManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TicketModel {

    private final TicketManager ticketManager;
    private ObservableList<TicketSold> ticketSoldToBeViewed;
    private ObservableList<Tickets> ticketToBeViewed;
    private Tickets currentTicket;
    private static volatile TicketModel instance;

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
    public void ticketsUser(be.Tickets tickets) throws Exception { // changes the ticket you are viewing and inserts the relevant user
        ticketSoldToBeViewed.clear();
        ticketSoldToBeViewed.addAll(ticketManager.getAllSoldTickets(tickets));
    }
    public ObservableList<TicketSold> getObservableSoldTickets() {return ticketSoldToBeViewed;}

    public TicketSold createNewSoldTicket(TicketSold newTicketSold) throws Exception {
        TicketSold ticketSold;
        ticketSold = ticketManager.createNewSoldTicket(newTicketSold);
        return ticketSold;
    }

    public void deleteSoldTicket(TicketSold selectedTicketSold) throws Exception {
        ticketManager.deleteSoldTicket(selectedTicketSold);
    }

    //This is for Ticket Code
    public boolean checkLocalTicketCode(String code, int ticketID) throws Exception {
        return ticketManager.checkLocalTicketCode(code, ticketID);
    }
    public boolean checkLocalTicketAllCode(String code) throws Exception {
        return ticketManager.checkLocalTicketAllCode(code);
    }
    public void createNewSoldTicketCode(TicketSold newTicketSoldCode) throws Exception {
        ticketManager.createNewSoldTicketCode(newTicketSoldCode);
    }
    public String readNewSoldTicketCode(TicketSold selectedTicket) throws Exception {
        return ticketManager.readNewSoldTicketCode(selectedTicket);
    }
    public void deleteSoldTicketCode(TicketSold selectedTicket) throws Exception {
        ticketManager.deleteSoldTicketCode(selectedTicket);
    }

    //This is for Ticket
    public void createNewTicket(Tickets tickets) throws Exception {
        Tickets newUser = ticketManager.createNewTicket(tickets);
        ticketToBeViewed.add(newUser);
    }
    public Tickets readTicket(int tickets) throws Exception{ return ticketManager.getTicket(tickets);}
    public void updateTicket(Tickets tickets) throws Exception{ ticketManager.updateTicket(tickets);}

    public Tickets getCurrentTicket() { return currentTicket; }
    public void setCurrentTicket(Tickets tickets) {currentTicket = tickets;}
    // This for global ticket code
    public boolean checkGlobalTicketCode(String code) throws Exception { return ticketManager.checkGlobalTicketCode(code); }
    public String generateNewGlobalTicketCode(TicketSold ticketSold) throws Exception { return ticketManager.generateNewGlobalTicketCode(ticketSold); }
}

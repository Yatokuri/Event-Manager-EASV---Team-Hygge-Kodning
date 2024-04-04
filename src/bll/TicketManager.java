package bll;

import be.TicketSold;
import be.Tickets;
import dal.db.Ticket_DB;

import java.util.Collection;
public class TicketManager {
    private final Ticket_DB ticket_DB;
    private ImageManager imageManager;

    public TicketManager() throws Exception {
        ticket_DB = new Ticket_DB();
        imageManager = new ImageManager();
    }

    public TicketSold createNewSoldTicket(TicketSold newTicketSold) throws Exception { return ticket_DB.createNewSoldTicket(newTicketSold); }

    public TicketSold getSoldTicket(TicketSold ticketSoldToFetch) throws Exception { return ticket_DB.fetchSoldTicket(ticketSoldToFetch); }

    public void updateSoldTicket(TicketSold selectedTicketSold) throws Exception { ticket_DB.updateSoldTicket(selectedTicketSold); }

    public void deleteSoldTicket(TicketSold selectedTicketSold) throws Exception { ticket_DB.deleteSoldTicket(selectedTicketSold); }


    public Collection<Tickets> getAllTickets() throws Exception { return ticket_DB.getAllTicket();}
    public void getTicket(Tickets selectedTicket) throws Exception { ticket_DB.getTicket(selectedTicket); }
    public void updateTicket(Tickets selectedTicket) throws Exception { ticket_DB.updateTicket(selectedTicket); }
    public void deleteTicket(Tickets selectedTicket) throws Exception {
        imageManager.deleteSystemIMG(selectedTicket.getTicketID());
        ticket_DB.deleteTicket(selectedTicket);
    }
    public Tickets createNewTicket(Tickets selectedTicket) throws Exception { return ticket_DB.createNewTicket(selectedTicket); }

}
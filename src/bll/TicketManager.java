package bll;

import be.Ticket;
import dal.db.Ticket_DB;

public class TicketManager {
    private final Ticket_DB ticket_DB;

    public TicketManager() throws Exception { ticket_DB = new Ticket_DB();  }

    public Ticket createNewTicket(Ticket newTicket) throws Exception { return ticket_DB.createNewTicket(newTicket); }

    public Ticket getTicket(Ticket ticketToFetch) throws Exception { return ticket_DB.fetchTicket(ticketToFetch); }

    public void updateTicket(Ticket selectedTicket) throws Exception { ticket_DB.updateTicket(selectedTicket); }

    public void deleteTicket(Ticket selectedTicket) throws Exception { ticket_DB.deleteTicket(selectedTicket); }


}
package gui.model;

import be.Ticket;
import bll.TicketManager;

public class TicketModel {

    private final TicketManager ticketManager;

    public TicketModel() throws Exception{
        ticketManager = new TicketManager();
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

package bll;

import be.TicketSold;
import be.Tickets;
import dal.db.Ticket_DB;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;
public class TicketManager {
    private final Ticket_DB ticket_DB;
    private ImageManager imageManager;
    private static final String ALPHANUMERIC_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"; // Character there can be used in the code
    private static final SecureRandom secureRandom = new SecureRandom();
    public TicketManager() throws Exception {
        ticket_DB = new Ticket_DB();
        imageManager = new ImageManager();
    }

    public void createNewSoldTicketCode(TicketSold newTicketSold) throws Exception {
        String idPart = String.valueOf(newTicketSold.getTransactionID());
        String randomPart = generateRandomAlphanumeric(15);
        String code = idPart + "." + randomPart;
        ticket_DB.createNewSoldTicketCode(code, newTicketSold);
    }
    public String readNewSoldTicketCode(TicketSold selectedTicket) throws Exception {return ticket_DB.readNewSoldTicketCode(selectedTicket);}
    public void updateCodeOnTicket(TicketSold selectedTicket) throws Exception {ticket_DB.updateNewSoldTicketCode(selectedTicket);}
    public void deleteSoldTicketCode(TicketSold selectedTicket) throws Exception {ticket_DB.deleteSoldTicketCode(selectedTicket);}
    public void deleteAllCodeOnTicket(Tickets selectedTicketID) throws Exception {ticket_DB.deleteAllCodeOnTicket(selectedTicketID);}

    public TicketSold createNewSoldTicket(TicketSold newTicketSold) throws Exception {return ticket_DB.createNewSoldTicket(newTicketSold);}
    public TicketSold getSoldTicket(TicketSold ticketSoldToFetch) throws Exception { return ticket_DB.fetchSoldTicket(ticketSoldToFetch); }

    public void updateSoldTicket(TicketSold selectedTicketSold) throws Exception { ticket_DB.updateSoldTicket(selectedTicketSold); }

    public void deleteSoldTicket(TicketSold selectedTicketSold) throws Exception { ticket_DB.deleteSoldTicket(selectedTicketSold); }
    public List<TicketSold> getAllSoldTickets(Tickets tickets) throws Exception {return ticket_DB.getAllSoldTickets(tickets);}

    public Collection<Tickets> getAllTickets() throws Exception { return ticket_DB.getAllTicket();}
    public void getTicket(Tickets selectedTicket) throws Exception { ticket_DB.getTicket(selectedTicket); }
    public void updateTicket(Tickets selectedTicket) throws Exception { ticket_DB.updateTicket(selectedTicket); }
    public void deleteTicket(Tickets selectedTicket) throws Exception {
        imageManager.deleteSystemIMG(selectedTicket.getTicketID());
        ticket_DB.deleteTicket(selectedTicket);
    }
    public Tickets createNewTicket(Tickets selectedTicket) throws Exception { return ticket_DB.createNewTicket(selectedTicket); }

    private static String generateRandomAlphanumeric(int length) { //Helper method to generate code
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(ALPHANUMERIC_CHARACTERS.length());
            sb.append(ALPHANUMERIC_CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }

}
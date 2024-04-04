package dal.db;

import be.Event;
import be.Tickets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EventTickets_DB {

    private final myDBConnector myDBConnector;
    private final Ticket_DB ticket_DB;

    public EventTickets_DB() throws Exception {
        myDBConnector = new myDBConnector();
        ticket_DB = new Ticket_DB();
    }

    public List<Tickets> getAllTicketsForEvent(Event event) throws Exception {
        ArrayList<Tickets> allTicketsForEvent = new ArrayList<>();
        String sql = "SELECT Tickets.* FROM Tickets JOIN dbo.EventTickets ON Tickets.TicketID = EventTickets.TicketID WHERE EventTickets.EventID = ?";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, event.getEventID());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Tickets ticket = generateTicket(rs);
                allTicketsForEvent.add(ticket);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new Exception("Could not get tickets for event from database", ex);
        }
        return allTicketsForEvent;
    }


    public void addTicketToEvent(Tickets ticket, Event event) throws Exception {
        String sql = "INSERT INTO dbo.EventTickets (EventID, TicketID) VALUES (?, ?)";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, event.getEventID());
            stmt.setInt(2, ticket.getTicketID());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new Exception("Could not add ticket to event", ex);
        }
    }

    public void removeTicketFromEvent(Tickets ticket, Event event) throws Exception {
        String sql = "DELETE FROM dbo.EventTickets WHERE EventID = ? AND TicketID = ?";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, event.getEventID());
            stmt.setInt(2, ticket.getTicketID());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new Exception("Could not remove ticket from event", ex);
        }
    }



    public void updateTicketInEvent(Tickets ticket, Tickets oldTicket, Event event) throws Exception {
        String sql = "UPDATE dbo.EventTickets SET TicketID = ? WHERE TicketID = ? AND EventID = ?";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ticket.getTicketID());
            stmt.setInt(2, oldTicket.getTicketID());
            stmt.setInt(3, event.getEventID());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new Exception("Could not update ticket", ex);
        }
    }

    public void deleteAllTicketFromEvent(Event event) throws Exception {
        String sql = "DELETE FROM dbo.EventTickets WHERE EventID = ?";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, event.getEventID());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new Exception("Could not delete all ticket from event", ex);
        }
    }

    // Helper method to generate a Ticket object from the ResultSet
    private Tickets generateTicket(ResultSet rs) throws SQLException {
        int ticketID = rs.getInt("TicketID");
        int ticketQuantity = rs.getInt("TicketQuantity");
        String ticketName = rs.getString("TicketName");
        String ticketJSON = rs.getString("TicketJSON");
        return new Tickets(ticketID, ticketQuantity, ticketName, ticketJSON);
    }

}
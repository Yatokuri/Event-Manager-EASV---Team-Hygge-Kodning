package dal.db;

import be.Tickets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GlobalTickets_DB {

    private final myDBConnector myDBConnector;

    public GlobalTickets_DB() throws Exception {
        myDBConnector = new myDBConnector();
    }

    //***************************CRUD*GLOBAL*TICKET*******************************
    public void addTicketToGlobal(Tickets tickets) throws Exception {
        String sql = "INSERT INTO dbo.GlobalTickets (TicketID) VALUES (?)";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tickets.getTicketID());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new Exception("Could not add ticket to global tickets", ex);
        }
    }
    public List<Tickets> getAllGlobalTicket() throws Exception {
        ArrayList<Tickets> allGlobalTickets = new ArrayList<>();
        String sql = "SELECT Tickets.* FROM Tickets JOIN GlobalTickets ON Tickets.TicketID = GlobalTickets.TicketID";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Tickets ticket = generateTicket(rs);
                allGlobalTickets.add(ticket);
            }
        } catch (SQLException ex) {
            throw new Exception("Could not get global tickets from database", ex);
        }
        return allGlobalTickets;
    }
    public void removeTicketFromGlobal(Tickets tickets) throws Exception {
        String sql = "DELETE FROM dbo.GlobalTickets WHERE TicketID = ?";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tickets.getTicketID());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new Exception("Could not remove ticket from event", ex);
        }
    }

//***************************HELPER*METHOD************************************
    private Tickets generateTicket(ResultSet rs) throws SQLException {
        int ticketID = rs.getInt("TicketID");
        int ticketQuantity = rs.getInt("TicketQuantity");
        String ticketName = rs.getString("TicketName");
        String ticketJSON = rs.getString("TicketJSON");
        int ticketLocal = rs.getInt("TicketLocal");
        return new Tickets(ticketID, ticketQuantity, ticketName, ticketJSON, ticketLocal);
    }

}
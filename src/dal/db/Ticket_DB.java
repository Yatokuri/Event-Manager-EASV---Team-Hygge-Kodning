package dal.db;

import be.Ticket;

import java.sql.*;

public class Ticket_DB {

    private final myDBConnector myDBConnector;

    public Ticket_DB() throws Exception {
        myDBConnector = new myDBConnector();
    }


    public Ticket createNewTicket(Ticket newTicket) throws Exception {
        String sql = "INSERT INTO dbo.Ticket (BuyerFirstName, BuyerLastName, BuyerEmail, TicketID, TicketEventID) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            // Bind parameters
            stmt.setString(1, newTicket.getFirstName());
            stmt.setString(2, newTicket.getLastName());
            stmt.setString(3, newTicket.getEmail());
            stmt.setInt(4, newTicket.getTicketID());
            stmt.setInt(5, newTicket.getTicketEventID()); // Missing reference to Events
            // Run the specified SQL statement
            stmt.executeUpdate();
            // Get the generated ID from the DB
            ResultSet rs = stmt.getGeneratedKeys();
            int id = 0;
            if (rs.next()) {
                id = rs.getInt(1);
            }

            // Create Movie object and send up the layers
            return new Ticket(newTicket.getFirstName(),
                    newTicket.getLastName(),
                    newTicket.getEmail(),
                    newTicket.getTicketID(),
                    newTicket.getTicketEventID(),
                    id);
        }
        catch (SQLException ex)
        {
            throw new Exception("Could not create Ticket", ex);
        }
    }

    public Ticket fetchTicket(Ticket ticketToFetch) throws Exception {
        String sql = "SELECT * FROM dbo.Ticket WHERE BuyerID = ?";
        try (Connection conn = myDBConnector.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, ticketToFetch.getBuyerID());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            return new Ticket(rs.getString("BuyerFirstName"),
                    rs.getString("BuyerLastName"),
                    rs.getString("BuyerEmail"),
                    rs.getInt("TicketID"),
                    rs.getInt("TicketEventID"),
                    rs.getInt("BuyerID"));
        }
        catch (SQLException ex){
            throw new Exception("Could not fetch Ticket", ex);
        }
    }

    public void updateTicket(Ticket ticket) throws Exception {
        String sql = "UPDATE dbo.Ticket SET BuyerFirstName = ?, BuyerLastName = ?, BuyerEmail = ? WHERE BuyerID = ?";

        try (Connection conn = myDBConnector.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, ticket.getFirstName());
            stmt.setString(2, ticket.getLastName());
            stmt.setString(3, ticket.getEmail());
            stmt.setInt(4, ticket.getTicketID());
            stmt.executeUpdate();
        }
        catch (SQLException ex){
            throw new Exception("Could not update Ticket", ex);
        }
    }

    public void deleteTicket(Ticket ticketToDelete) throws Exception {
        String sql = "DELETE FROM dbo.Ticket WHERE BuyerID = ?";

        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ticketToDelete.getBuyerID());
            stmt.executeUpdate();
        }
        catch (SQLException ex){
            throw new Exception("Could not Delete Ticket", ex);
        }
    }
}

package dal.db;

import be.TicketSold;
import be.Tickets;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Ticket_DB {

    private final myDBConnector myDBConnector;

    public Ticket_DB() throws Exception {
        myDBConnector = new myDBConnector();
        allTickets = new ArrayList<>();
    }


    public TicketSold createNewSoldTicket(TicketSold newTicketSold) throws Exception {
        String sql = "INSERT INTO dbo.Ticket (BuyerFirstName, BuyerLastName, BuyerEmail, TicketID, TicketEventID) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            // Bind parameters
            stmt.setString(1, newTicketSold.getFirstName());
            stmt.setString(2, newTicketSold.getLastName());
            stmt.setString(3, newTicketSold.getEmail());
            stmt.setInt(4, newTicketSold.getTicketID());
            stmt.setInt(5, newTicketSold.getTicketEventID()); // Missing reference to Events
            // Run the specified SQL statement
            stmt.executeUpdate();
            // Get the generated ID from the DB
            ResultSet rs = stmt.getGeneratedKeys();
            int id = 0;
            if (rs.next()) {
                id = rs.getInt(1);
            }

            // Create Movie object and send up the layers
            return new TicketSold(newTicketSold.getFirstName(),
                    newTicketSold.getLastName(),
                    newTicketSold.getEmail(),
                    newTicketSold.getTicketID(),
                    newTicketSold.getTicketEventID(),
                    id);
        }
        catch (SQLException ex)
        {
            throw new Exception("Could not create Ticket", ex);
        }
    }

    public TicketSold fetchSoldTicket(TicketSold ticketSoldToFetch) throws Exception {
        String sql = "SELECT * FROM dbo.Ticket WHERE BuyerID = ?";
        try (Connection conn = myDBConnector.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, ticketSoldToFetch.getBuyerID());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            return new TicketSold(rs.getString("BuyerFirstName"),
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

    public void updateSoldTicket(TicketSold ticketSold) throws Exception {
        String sql = "UPDATE dbo.Ticket SET BuyerFirstName = ?, BuyerLastName = ?, BuyerEmail = ? WHERE BuyerID = ?";

        try (Connection conn = myDBConnector.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, ticketSold.getFirstName());
            stmt.setString(2, ticketSold.getLastName());
            stmt.setString(3, ticketSold.getEmail());
            stmt.setInt(4, ticketSold.getTicketID());
            stmt.executeUpdate();
        }
        catch (SQLException ex){
            throw new Exception("Could not update Ticket", ex);
        }
    }

    public void deleteSoldTicket(TicketSold ticketSoldToDelete) throws Exception {
        String sql = "DELETE FROM dbo.Ticket WHERE BuyerID = ?";

        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ticketSoldToDelete.getBuyerID());
            stmt.executeUpdate();
        }
        catch (SQLException ex){
            throw new Exception("Could not Delete Ticket", ex);
        }
    }

    private static ArrayList<Tickets> allTickets;

    public List<Tickets> getAllTicket() throws Exception {
        List<Tickets> allTickets = new ArrayList<>();
        String sql = "SELECT * FROM dbo.Tickets;";
        try (Connection conn = myDBConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                allTickets.add(generateTicket(rs));
            }
            return allTickets;
        } catch (SQLException ex) {
            throw new Exception("Could not fetch tickets from database", ex);
        }
    }

    public Tickets createNewTicket(Tickets newTicket) throws Exception {
        String sql = "INSERT INTO dbo.Tickets (TicketQuantity, TicketName, TicketJSON) VALUES (?, ?, ?)";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, newTicket.getTicketQuantity());
            pstmt.setString(2, newTicket.getTicketName());
            pstmt.setString(3, newTicket.getTicketJSON());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    newTicket.setTicketID(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating ticket failed, no ID obtained.");
                }
            }
            return newTicket;
        } catch (SQLException ex) {
            throw new Exception("Could not create ticket in database", ex);
        }
    }

    public void updateTicket(Tickets updatedTicket) throws Exception {
        String sql = "UPDATE dbo.Tickets SET TicketQuantity = ?, TicketName = ?, TicketJSON = ? WHERE TicketID = ?";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, updatedTicket.getTicketQuantity());
            stmt.setString(2, updatedTicket.getTicketName());
            stmt.setString(3, updatedTicket.getTicketJSON());
            stmt.setInt(4, updatedTicket.getTicketID());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new Exception("Could not update ticket", ex);
        }
    }

    public void deleteTicket(Tickets selectedTicket) throws Exception {
        String sql = "DELETE FROM dbo.Tickets WHERE TicketID = ?";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, selectedTicket.getTicketID());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new Exception("Could not delete ticket from database", ex);
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

    public Tickets getTicket(Tickets ticketToFetch) throws Exception {
        Tickets ticket;
        String sql = "SELECT * FROM dbo.Tickets WHERE TicketID = ?";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ticketToFetch.getTicketID());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    ticket = generateTicket(rs);
                } else {
                    throw new SQLException("Ticket not found in database");
                }
            }
        } catch (SQLException ex) {
            throw new Exception("Could not fetch ticket from database", ex);
        }
        return ticket;
    }

}


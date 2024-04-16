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
    }

//*****************************CRUD*TICKET*CODE*******************************
    public void createNewSoldTicketCode(String code, TicketSold newTicketSold) throws Exception {
        String sql = "INSERT INTO dbo.Codes (Code, TicketID, TransactionID) VALUES (?, ?, ?)";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            // Bind parameters
            stmt.setString(1, code);
            stmt.setInt(2, newTicketSold.getTicketID());
            stmt.setInt(3, newTicketSold.getTransactionID());
            // Execute the insert
            stmt.executeUpdate();
        }
        catch (SQLException ex)
        {
            throw new Exception("Could not create Code", ex);
        }
    }

    public void generateNewGlobalTicketCode(String code) throws Exception {
        String sql = "INSERT INTO dbo.GlobalCodes (GlobalCodes) VALUES (?)";

        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            stmt.executeUpdate(); // Insert the new record into the table
        } catch (SQLException ex) {
            throw new Exception("Error while generating new code", ex);
        }
    }

    public String readNewSoldTicketCode(TicketSold ticketSoldToFetch) throws Exception {
        String sql = "SELECT * FROM dbo.Codes WHERE TransactionID = ?";
        try (Connection conn = myDBConnector.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, ticketSoldToFetch.getTransactionID());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("Code");
            } else {
                throw new Exception("No code found");
            }
        } catch (SQLException ex) {
            throw new Exception("Could not read Code", ex);
        }
    }
    public boolean checkGlobalTicketCode(String code) throws Exception {
        String sqlCheck = "SELECT * FROM dbo.GlobalCodes WHERE GlobalCodes = ?";
        String sqlDelete = "DELETE FROM dbo.GlobalCodes WHERE GlobalCodes = ?";

        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmtCheck = conn.prepareStatement(sqlCheck);
             PreparedStatement stmtDelete = conn.prepareStatement(sqlDelete)) {

            stmtCheck.setString(1, code);
            ResultSet rs = stmtCheck.executeQuery();

            if (rs.next()) {
                // Record exists, return true
                stmtDelete.setString(1, code);
                stmtDelete.executeUpdate(); // Remove the record from the table
                return true;
            } else {
                // No record found, return false
                return false;
            }
        } catch (SQLException ex) {
            throw new Exception("Error while checking and deleting record", ex);
        }
    }
    public boolean checkLocalTicketCode(String code, int ticketID) throws Exception {
        String sqlCheck = "SELECT Code FROM dbo.Codes WHERE Code = ? AND TicketID = ?";

        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmtCheck = conn.prepareStatement(sqlCheck)) {

            stmtCheck.setString(1, code);
            stmtCheck.setInt(2, ticketID);
            ResultSet rs = stmtCheck.executeQuery();

            return rs.next(); // Return true if the ResultSet has at least one row, indicating the record exists
        } catch (SQLException ex) {
            throw new Exception("Error while checking code", ex);
        }
    }

    public boolean checkLocalTicketAllCode(String code) throws Exception {
        String sqlCheck = "SELECT Code FROM dbo.Codes WHERE Code = ?";

        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmtCheck = conn.prepareStatement(sqlCheck)) {

            stmtCheck.setString(1, code);
            ResultSet rs = stmtCheck.executeQuery();

            return rs.next(); // Return true if the ResultSet has at least one row, indicating the record exists
        } catch (SQLException ex) {
            throw new Exception("Error while checking code", ex);
        }
    }
    public void updateNewSoldTicketCode(TicketSold ticketSoldToUpdate) throws Exception {
        String sql = "UPDATE dbo.Codes SET BuyerFirstName = ?, BuyerLastName = ?, BuyerEmail = ? WHERE TransactionID = ?";

        try (Connection conn = myDBConnector.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, ticketSoldToUpdate.getFirstName());
            stmt.setString(2, ticketSoldToUpdate.getLastName());
            stmt.setString(3, ticketSoldToUpdate.getEmail());
            stmt.setInt(4, ticketSoldToUpdate.getTicketID());
            stmt.executeUpdate();
        }
        catch (SQLException ex){
            throw new Exception("Could not update Ticket", ex);
        }
    }

    public void deleteSoldTicketCode(TicketSold ticketSoldToDelete) throws Exception {
        String sql = "DELETE FROM dbo.Codes WHERE TransactionID = ?";

        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ticketSoldToDelete.getTransactionID());
            stmt.executeUpdate();
        }
        catch (SQLException ex){
            throw new Exception("Could not delete Code", ex);
        }
    }

    public void deleteAllCodeOnTicket(Tickets ticketSoldToDelete) throws Exception {
        String sql = "DELETE FROM dbo.Codes WHERE TicketID = ?";

        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ticketSoldToDelete.getTicketID());
            stmt.executeUpdate();
        }
        catch (SQLException ex){
            throw new Exception("Could not delete Codes", ex);
        }
    }

//*****************************CRUD*SOLD*TICKET******************************
    public TicketSold createNewSoldTicket(TicketSold newTicketSold) throws Exception {
        String sql = "INSERT INTO dbo.TicketSold (BuyerFirstName, BuyerLastName, BuyerEmail, TicketID) VALUES (?, ?, ?, ?)";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            // Bind parameters
            stmt.setString(1, newTicketSold.getFirstName());
            stmt.setString(2, newTicketSold.getLastName());
            stmt.setString(3, newTicketSold.getEmail());
            stmt.setInt(4, newTicketSold.getTicketID());
            // Run the specified SQL statement
            stmt.executeUpdate();
            // Get the generated ID from the DB
            ResultSet rs = stmt.getGeneratedKeys();
            int id = 0;
            if (rs.next()) {
                id = rs.getInt(1);
            }

            // Create Ticket object and send up the layers
            return new TicketSold(newTicketSold.getFirstName(),
                    newTicketSold.getLastName(),
                    newTicketSold.getEmail(),
                    newTicketSold.getTicketID(),
                    id);
        }
        catch (SQLException ex)
        {
            throw new Exception("Could not create Ticket", ex);
        }
    }

    public TicketSold fetchSoldTicket(TicketSold ticketSoldToFetch) throws Exception {
        String sql = "SELECT * FROM dbo.TicketSold WHERE TransactionID = ?";
        try (Connection conn = myDBConnector.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, ticketSoldToFetch.getTransactionID());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            return new TicketSold(rs.getString("BuyerFirstName"),
                    rs.getString("BuyerLastName"),
                    rs.getString("BuyerEmail"),
                    rs.getInt("TicketID"),
                    rs.getInt("TransactionID"));
        }
        catch (SQLException ex){
            throw new Exception("Could not fetch Ticket", ex);
        }
    }

    public List<TicketSold> getAllSoldTickets(Tickets tickets) throws Exception {
        ArrayList<TicketSold> allTicketsForEvent = new ArrayList<>();
        String sql = "SELECT * FROM dbo.TicketSold WHERE TicketID = ?";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tickets.getTicketID());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TicketSold ticketSold = generateTicketSold(rs);
                allTicketsForEvent.add(ticketSold);
            }
        } catch (SQLException ex) {
            throw new Exception("Could not get tickets sold", ex);
        }
        return allTicketsForEvent;
    }

    public void updateSoldTicket(TicketSold ticketSold) throws Exception {
        String sql = "UPDATE dbo.TicketSold SET BuyerFirstName = ?, BuyerLastName = ?, BuyerEmail = ? WHERE TransactionID = ?";

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
        String sql = "DELETE FROM dbo.TicketSold WHERE TransactionID = ?";

        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ticketSoldToDelete.getTransactionID());
            stmt.executeUpdate();
        }
        catch (SQLException ex){
            throw new Exception("Could not delete Ticket", ex);
        }
    }

    public void deleteAllSoldOfOneTicket(int ticketSoldToDelete) throws Exception {
        String sql = "DELETE FROM dbo.TicketSold WHERE TicketID = ?";

        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ticketSoldToDelete);
            stmt.executeUpdate();
        }
        catch (SQLException ex){
            throw new Exception("Could not Delete Tickets", ex);
        }
    }

//*****************************CRUD*TICKET***********************************

    public Tickets createNewTicket(Tickets newTicket) throws Exception {
        String sql = "INSERT INTO dbo.Tickets (TicketQuantity, TicketName, TicketJSON, TicketLocal) VALUES (?, ?, ?, ?)";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement pStmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pStmt.setInt(1, newTicket.getTicketQuantity());
            pStmt.setString(2, newTicket.getTicketName());
            pStmt.setString(3, newTicket.getTicketJSON());
            pStmt.setInt(4, newTicket.getIsILocal());
            pStmt.executeUpdate();

            try (ResultSet generatedKeys = pStmt.getGeneratedKeys()) {
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

    public Tickets getTicket(int ticketToFetch) throws Exception {
        Tickets ticket;
        String sql = "SELECT * FROM dbo.Tickets WHERE TicketID = ?";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement pStmt = conn.prepareStatement(sql)) {
            pStmt.setInt(1, ticketToFetch);
            try (ResultSet rs = pStmt.executeQuery()) {
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

    public void updateTicket(Tickets updatedTicket) throws Exception {
        String sql = "UPDATE dbo.Tickets SET TicketQuantity = ?, TicketName = ?, TicketJSON = ?, TicketLocal = ? WHERE TicketID = ?";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, updatedTicket.getTicketQuantity());
            stmt.setString(2, updatedTicket.getTicketName());
            stmt.setString(3, updatedTicket.getTicketJSON());
            stmt.setInt(4, updatedTicket.getIsILocal());
            stmt.setInt(5, updatedTicket.getTicketID());
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

//***************************HELPER*METHOD************************************
    // Helper method to generate a Ticket object from the ResultSet
    private Tickets generateTicket(ResultSet rs) throws SQLException {
        int ticketID = rs.getInt("TicketID");
        int ticketQuantity = rs.getInt("TicketQuantity");
        String ticketName = rs.getString("TicketName");
        String ticketJSON = rs.getString("TicketJSON");
        int ticketLocal = rs.getInt("TicketLocal");
        return new Tickets(ticketID, ticketQuantity, ticketName, ticketJSON, ticketLocal);
    }

    // Helper method to generate a SoldTicket object from the ResultSet
    public static TicketSold generateTicketSold(ResultSet rs) throws SQLException {
        String firstName = rs.getString("BuyerFirstName");
        String lastName = rs.getString("BuyerLastName");
        String email = rs.getString("BuyerEmail");
        int ticketID = rs.getInt("TicketID");
        int transactionID = rs.getInt("TransactionID");
        return new TicketSold(firstName, lastName, email, ticketID, transactionID);
    }

}


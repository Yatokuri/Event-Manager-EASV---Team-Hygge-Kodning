package dal.db;

import be.Event;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArchivedEvent_DB {
    private final myDBConnector myDBConnector;
    private static ArrayList<Event> allArchivedEvents;

    public ArchivedEvent_DB() throws Exception {
        myDBConnector = new myDBConnector();
        allArchivedEvents = new ArrayList<>();
        getAllArchivedEvents();
    }

//*****************************CRUD*EVENT*ARCHIVED*******************************

    public be.Event ArchiveEvent(be.Event event) throws Exception {
        String sql = "INSERT INTO dbo.ArchivedEvents (ArchivedEventName, ArchivedEventStart, ArchivedEventEnd, ArchivedLocation, ArchivedLocationGuidance, ArchivedEventNotes) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            // Bind parameters
            stmt.setString(1, event.getEventName());
            stmt.setString(2, event.getEventStartDateTime());
            stmt.setString(3, event.getEventEndDateTime());
            stmt.setString(4, event.getLocation());
            stmt.setString(5, event.getLocationGuidance());
            stmt.setString(6, event.getEventNotes());
            // Run the specified SQL statement
            stmt.executeUpdate();
            // Get the generated ID from the DB
            ResultSet rs = stmt.getGeneratedKeys();
            int id = 0;
            if (rs.next()) {
                id = rs.getInt(1);
            }

            // Create Event object and send up the layers
            Event newEvent = new Event(event.getEventName(), event.getEventStartDateTime(), event.getEventEndDateTime(), event.getLocation(), event.getLocationGuidance(), event.getEventNotes(), id , 0);
            allArchivedEvents.add(newEvent);
            return newEvent;
        }
        catch (SQLException ex)
        {
            throw new Exception("Could not Archive Event", ex);
        }

    }

    public List<Event> getAllArchivedEvents() throws Exception {
        allArchivedEvents.clear();
        try (Connection conn = myDBConnector.getConnection();
             Statement stmt = conn.createStatement())
        {
            String sql = "SELECT * FROM dbo.ArchivedEvents;";
            ResultSet rs = stmt.executeQuery(sql);
            // Loop through rows from the database result set
            while (rs.next()) {
                allArchivedEvents.add(generateArchivedEvent(rs));
            }
            return allArchivedEvents;
        }
        catch (SQLException ex)
        {
            throw new Exception("Could not get Archived Events from database", ex);
        }
    }

    public void deleteArchivedEvent(be.Event eventToDelete) throws Exception {
        String sql = "DELETE FROM dbo.ArchivedEvents WHERE ArchivedEventID = ?";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, eventToDelete.getEventID());
            stmt.executeUpdate();
            allArchivedEvents.remove(eventToDelete);
        }
        catch (SQLException ex){
            throw new Exception("Could not delete Archived Event", ex);
        }
    }

//***************************HELPER*METHOD************************************
    private be.Event generateArchivedEvent(ResultSet rs) throws SQLException {
        String archivedEventName = rs.getString("ArchivedEventName");
        String archivedEventStart = rs.getString("ArchivedEventStart");
        String archivedEventEnd = rs.getString("ArchivedEventEnd");
        String archivedLocation = rs.getString("ArchivedLocation");
        String archivedLocationGuidance = rs.getString("ArchivedLocationGuidance");
        String archivedEventNotes = rs.getString("ArchivedEventNotes");
        int archivedEventID = rs.getInt("ArchivedEventID");
        return new Event(archivedEventName, archivedEventStart, archivedEventEnd, archivedLocation, archivedLocationGuidance, archivedEventNotes, archivedEventID, 0);
    }

}

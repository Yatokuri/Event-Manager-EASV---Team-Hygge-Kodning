package dal.db;

import be.Event;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Event_DB {

    private final myDBConnector myDBConnector;
    private static ArrayList<be.Event> allEvents;

    public Event_DB() throws Exception {
        myDBConnector = new myDBConnector();
        allEvents = new ArrayList<>();
        getAllEvents();
    }

    public List<be.Event> getAllEvents() throws Exception {
        allEvents.clear();
        try (Connection conn = myDBConnector.getConnection();
             Statement stmt = conn.createStatement())
        {
            String sql = "SELECT * FROM dbo.Events;";
            ResultSet rs = stmt.executeQuery(sql);
            // Loop through rows from the database result set
            while (rs.next()) {
                allEvents.add(generateEvent(rs));
            }
            return allEvents;
        }
        catch (SQLException ex)
        {
            throw new Exception("Could not get Events from database", ex);
        }
    }

    public be.Event createEvent(be.Event event) throws Exception {
        String sql = "INSERT INTO dbo.Events (EventName, EventStart, EventEnd, Location, LocationGuidance, EventNotes) VALUES (?, ?, ?, ?, ?, ?)";

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

            // Create Movie object and send up the layers
            Event newEvent = new Event(event.getEventName(), event.getEventStartDateTime(), event.getEventEndDateTime(), event.getLocation(), event.getLocationGuidance(), event.getEventNotes(), id);
            allEvents.add(newEvent);
            return newEvent;
        }
        catch (SQLException ex)
        {
            throw new Exception("Could not create Event", ex);
        }

    }

    public void updateEvent(be.Event event) throws Exception {
        String sql = "UPDATE dbo.Events SET EventName = ?, EventStart = ?, EventEnd = ?, Location = ?, LocationGuidance = ?, EventNotes = ? WHERE EventID = ?";

        try (Connection conn = myDBConnector.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, event.getEventName());
            stmt.setString(2, event.getEventStartDateTime());
            stmt.setString(3, event.getEventEndDateTime());
            stmt.setString(4, event.getLocation());
            stmt.setString(5, event.getLocationGuidance());
            stmt.setString(6, event.getEventNotes());
            stmt.setInt(7, event.getEventID());
            stmt.executeUpdate();
        }
        catch (SQLException ex){
            throw new Exception("Could not update Event", ex);
        }
    }

    public void deleteEvent(be.Event eventToDelete) throws Exception {
        String sql = "DELETE FROM dbo.Events WHERE EventID = ?";

        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, eventToDelete.getEventID());
            stmt.executeUpdate();
            allEvents.remove(eventToDelete);
        }
        catch (SQLException ex){
            throw new Exception("Could not Delete Event", ex);
        }
    }

    private be.Event generateEvent(ResultSet rs) throws SQLException {
        String eventName = rs.getString("EventName");
        String eventStart = rs.getString("EventStart");
        String eventEnd = rs.getString("EventEnd");
        String location = rs.getString("Location");
        String locationGuidance = rs.getString("LocationGuidance");
        String eventNotes = rs.getString("EventNotes");
        int eventID = rs.getInt("EventID");
        return new Event(eventName, eventStart, eventEnd, location, locationGuidance, eventNotes, eventID);
    }
}

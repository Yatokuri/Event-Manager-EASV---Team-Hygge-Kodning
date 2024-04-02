package bll;

import dal.db.Event_DB;

import java.util.List;

public class EventManager {

    private final Event_DB event_DB;

    public EventManager() throws Exception { event_DB = new Event_DB();  }

    public be.Event createNewEvent(be.Event newEvent) throws Exception { return event_DB.createEvent(newEvent); }

    public List<be.Event> getAllEvents() throws Exception { return event_DB.getAllEvents(); }

    public void updateEvent(be.Event selectedEvent) throws Exception { event_DB.updateEvent(selectedEvent); }

    public void deleteEvent(be.Event selectedEvent) throws Exception { event_DB.deleteEvent(selectedEvent); }
}

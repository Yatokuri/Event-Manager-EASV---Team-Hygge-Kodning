package bll;

import dal.db.Event_DB;
import gui.model.ImageModel;

import java.util.List;

public class EventManager {

    private final Event_DB event_DB;
    private final ImageModel imageModel;

    public EventManager() throws Exception {
        event_DB = new Event_DB();
        imageModel = ImageModel.getInstance();
    }

    public be.Event createNewEvent(be.Event newEvent) throws Exception { return event_DB.createEvent(newEvent); }

    public List<be.Event> getAllEvents() throws Exception { return event_DB.getAllEvents(); }

    public void updateEvent(be.Event selectedEvent) throws Exception { event_DB.updateEvent(selectedEvent); }

    public void deleteEvent(be.Event selectedEvent) throws Exception {
        if (selectedEvent.getImageID() != 0)    { //If any Image we delete it too
            imageModel.deleteSystemIMG(selectedEvent.getImageID());
        }
        event_DB.deleteEvent(selectedEvent); }
}

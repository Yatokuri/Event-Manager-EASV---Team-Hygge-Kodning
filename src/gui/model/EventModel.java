package gui.model;

import be.Event;
import bll.EventManager;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class EventModel {

    private final EventManager eventManager;

    private static ObservableList<be.Event> eventsToBeViewed;

    public EventModel() throws Exception{
        eventManager = new EventManager();
        eventsToBeViewed = FXCollections.observableArrayList();
        eventsToBeViewed.addAll(eventManager.getAllEvents());
    }

    public Event createNewEvent(be.Event newEvent) throws Exception {
        be.Event event;
        event = eventManager.createNewEvent(newEvent);
        return event;
    }

    public void updateEvent(be.Event updatedEvent) throws Exception {
        eventManager.updateEvent(updatedEvent);
    }

    public void deleteEvent(be.Event selectedEvent) throws Exception {
        eventManager.deleteEvent(selectedEvent);
    }
}

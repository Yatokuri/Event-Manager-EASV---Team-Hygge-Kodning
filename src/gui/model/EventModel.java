package gui.model;

import be.Event;
import bll.EventManager;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class EventModel {

    private final EventManager eventManager;

    private static ObservableList<be.Event> eventsToBeViewed;

    public EventModel() throws Exception{
        eventManager = new EventManager();
        eventsToBeViewed = FXCollections.observableArrayList();
        eventsToBeViewed.addAll(eventManager.getAllEvents());
    }
    public ObservableList<Event> getObsEvents() { return eventsToBeViewed; }

    public Event createNewEvent(be.Event newEvent) throws Exception {
        be.Event event;
        event = eventManager.createNewEvent(newEvent);
        eventsToBeViewed.add(event);
        return event;
    }

    public void updateEvent(be.Event updatedEvent) throws Exception {
        eventManager.updateEvent(updatedEvent);
        int index = eventsToBeViewed.indexOf(updatedEvent);
        if (index != -1) { // If the event is found in the observable list
            eventsToBeViewed.set(index, updatedEvent); // Replace the old event with the updated one
        }
    }

    public void deleteEvent(be.Event selectedEvent) throws Exception {
        eventManager.deleteEvent(selectedEvent);
        eventsToBeViewed.remove(selectedEvent);
    }
}

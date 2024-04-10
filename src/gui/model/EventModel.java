package gui.model;

import be.Event;
import bll.EventManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class EventModel {

    private final EventManager eventManager;

    private static ObservableList<be.Event> eventsToBeViewed;
    private static EventModel instance;

    private EventModel() throws Exception {
        eventManager = new EventManager();
        eventsToBeViewed = FXCollections.observableArrayList();
        eventsToBeViewed.addAll(eventManager.getAllEvents());
    }

    // Public method to get the singleton instance, so we have control over data
    public static EventModel getInstance() throws Exception {
        if (instance == null) {
            synchronized (EventModel.class) {
                if (instance == null) {
                    instance = new EventModel();
                }
            }
        }
        return instance;
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

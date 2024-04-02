package gui.model;

import be.Event;
import bll.ArchivedEventManager;
import bll.EventManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ArchivedEventModel {
    private final ArchivedEventManager archivedEventManager;

    private static ObservableList<Event> archivedEventsToBeViewed;
    private static ArchivedEventModel instance;

    private ArchivedEventModel() throws Exception {
        archivedEventManager = new ArchivedEventManager();
        archivedEventsToBeViewed = FXCollections.observableArrayList();
        archivedEventsToBeViewed.addAll(archivedEventManager.getAllEvents());
    }

    // Public method to get the singleton instance, so we have control over data
    public static ArchivedEventModel getInstance() throws Exception {
        if (instance == null) {
            synchronized (EventModel.class) {
                if (instance == null) {
                    instance = new ArchivedEventModel();
                }
            }
        }
        return instance;
    }

    public ObservableList<Event> getObsArchivedEvents() { return archivedEventsToBeViewed; }

    public Event archiveEvent(be.Event newEvent) throws Exception {
        be.Event archivedEvent;
        archivedEvent = archivedEventManager.archiveEvent(newEvent);
        archivedEventsToBeViewed.add(archivedEvent);
        return archivedEvent;
    }

    public void deleteEvent(be.Event selectedEvent) throws Exception {
        archivedEventManager.deleteEvent(selectedEvent);
        archivedEventsToBeViewed.remove(selectedEvent);
    }

}

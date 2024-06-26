package gui.model;

import be.Event;
import bll.ArchivedEventManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ArchivedEventModel {
    private final ArchivedEventManager archivedEventManager;

    private static ObservableList<Event> archivedEventsToBeViewed;
    private static volatile ArchivedEventModel instance;

    private ArchivedEventModel() throws Exception {
        archivedEventManager = new ArchivedEventManager();
        archivedEventsToBeViewed = FXCollections.observableArrayList();
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

    public ObservableList<Event> getArchivedEventsToBeViewed() throws Exception {
        archivedEventsToBeViewed.clear();
        archivedEventsToBeViewed.addAll(archivedEventManager.getAllArchivedEvents());
        return archivedEventsToBeViewed;
    }
    public ObservableList<Event> getObsArchivedEvents() { return archivedEventsToBeViewed; }

    public void archiveEvent(Event newEvent) throws Exception {
        be.Event archivedEvent;
        archivedEvent = archivedEventManager.archiveEvent(newEvent);
        archivedEventsToBeViewed.add(archivedEvent);
    }

    public void deleteEvent(be.Event selectedEvent) throws Exception {
        archivedEventManager.deleteEvent(selectedEvent);
        archivedEventsToBeViewed.remove(selectedEvent);
    }

}

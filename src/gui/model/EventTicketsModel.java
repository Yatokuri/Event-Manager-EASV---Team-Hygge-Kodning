package gui.model;

import be.Event;
import be.Tickets;
import bll.EventTicketsManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class EventTicketsModel {
    private EventModel eventModel;

    private static EventTicketsModel instance;
    private final EventTicketsManager eventTicketsManager;
    private final ObservableList<Tickets> eventTicketsToBeViewed;

    private EventTicketsModel() throws Exception {
        eventTicketsManager = new EventTicketsManager();
        eventTicketsToBeViewed = FXCollections.observableArrayList();
        eventModel = EventModel.getInstance();
        for (Event p: eventModel.getObsEvents()) {
            eventTicketsToBeViewed.addAll(eventTicketsManager.getAllTicketsEvent(p));
        }
    }



    public static EventTicketsModel getInstance() throws Exception {
        if (instance == null) {
            synchronized (UserModel.class) { if (instance == null) { instance = new EventTicketsModel(); } }
        }
        return instance;
    }

    public void eventTickets(be.Event event) throws Exception { // changes the event you are viewing and inserts the relevant ticketss
        eventTicketsToBeViewed.clear();
        eventTicketsToBeViewed.addAll(eventTicketsManager.getAllTicketsEvent(event));
    }
    public boolean addTicketsToEvent(Tickets newtickets, be.Event event) throws Exception { // Sends a request to the database to add a tickets to a event
        for (Tickets s : eventTicketsToBeViewed) {
            if (newtickets.getTicketID() == s.getTicketID()) {
                return false; // Exit the method fast
            }
        }
        eventTicketsManager.addTicketsToEvent(newtickets, event);
        eventTicketsToBeViewed.add(newtickets); // update list // Adds the new tickets to the event observable list
        return true;
    }

    public void updateTicketsInEvent (Tickets tickets, Tickets oldtickets, be.Event event) throws Exception { // Sends a request to the database to update a tickets in an event
        eventTicketsManager.updateTicketsInEvent(tickets, oldtickets, event);
    }

    public void deleteTicketsFromEvent (Tickets tickets, be.Event event) throws Exception { // Sends a request to the database to delete a tickets from a event
        eventTicketsManager.removeImageFromTickets(tickets); // Remove the IMG from db too
        eventTicketsManager.deleteTicketsFromEvent(tickets , event);
        eventTicketsToBeViewed.clear();
        eventTicketsToBeViewed.addAll(eventTicketsManager.getAllTicketsEvent(event)); // Updates the event observable list with the changes
    }

    public void deleteAllTicketsFromEvent (be.Event event) throws Exception { // Sends a request to the database to empty the event of all tickets
        eventTickets(event);
        for (Tickets t : eventTicketsToBeViewed) { // Remove the IMG from db too
            eventTicketsManager.removeImageFromTickets(t);
        }
        eventTicketsManager.deleteAllTicketsFromEvent(event);
    }
    public ObservableList<Tickets> getObservableEventsTickets() {return eventTicketsToBeViewed;} // Returns the event
}

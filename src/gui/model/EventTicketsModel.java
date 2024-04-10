package gui.model;

import be.Event;
import be.Tickets;
import bll.EventTicketsManager;
import bll.TicketManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class EventTicketsModel {
    private final EventModel eventModel;

    private static EventTicketsModel instance;
    private final EventTicketsManager eventTicketsManager;
    private final TicketManager ticketManager;
    private final ObservableList<Tickets> eventTicketsToBeViewed;

    private EventTicketsModel() throws Exception {
        eventTicketsManager = new EventTicketsManager();
        ticketManager = new TicketManager();
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

    public void eventTickets(be.Event event) throws Exception { // changes the event you are viewing and inserts the relevant tickets
        eventTicketsToBeViewed.clear();
        eventTicketsToBeViewed.addAll(eventTicketsManager.getAllTicketsEvent(event));
    }
    public boolean addTicketsToEvent(Tickets newtickets, be.Event event) throws Exception { // Sends a request to the database to add a tickets to an event
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

    public void deleteTicketsFromEvent (Tickets tickets, be.Event event) throws Exception { // Sends a request to the database to delete a tickets from an event
        eventTicketsManager.removeImageFromTickets(tickets); // Remove the IMG from db too
        eventTicketsManager.deleteTicketsFromEvent(tickets , event);
        ticketManager.deleteAllCodeOnTicket(tickets);
        ticketManager.deleteTicket(tickets);
        eventTicketsToBeViewed.clear();
        eventTicketsToBeViewed.addAll(eventTicketsManager.getAllTicketsEvent(event)); // Updates the event observable list with the changes
    }

    public void deleteAllTicketsFromEvent (be.Event event) throws Exception { // Sends a request to the database to empty the event of all tickets
        eventTickets(event);
        for (Tickets t : eventTicketsToBeViewed) { // Remove the IMG from db too
            eventTicketsManager.removeImageFromTickets(t);
        }
        eventTicketsManager.deleteAllTicketsFromEvent(event);
        for (Tickets t : eventTicketsToBeViewed) { // We delete stuff in right order so foreign key don't block it, Try catch makes sure it does not stop if nothing can be deleted
            try {
                ticketManager.deleteAllCodeOnTicket(t);
            } catch (Exception ignored) {
            }
            try {
                ticketManager.deleteAllSoldOfOneTicket(t.getTicketID());
            } catch (Exception ignored) {
            }
            try {
                ticketManager.deleteTicket(t);
            } catch (Exception ignored) {
            }
        }
        eventTicketsToBeViewed.clear();
        eventTicketsToBeViewed.addAll(eventTicketsManager.getAllTicketsEvent(event)); // Updates the event observable list with the changes
    }
    public ObservableList<Tickets> getObservableEventsTickets() {return eventTicketsToBeViewed;} // Returns the event
}

package bll;

import be.Event;
import be.Tickets;
import dal.db.EventTickets_DB;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;

public class EventTicketsManager {

    private final EventTickets_DB eventTickets_DB;
    private final ImageManager imageManager;

    public EventTicketsManager() throws Exception {
        eventTickets_DB = new EventTickets_DB();
        imageManager = new ImageManager();
    }

//*****************************CRUD*TICKET*EVENT*******************************
    public void addTicketsToEvent(Tickets newtickets, Event event) throws Exception {eventTickets_DB.addTicketToEvent(newtickets, event);}
    public List<Tickets> getAllTicketsEvent(Event event) throws Exception {return eventTickets_DB.getAllTicketsForEvent(event);}
    public void updateTicketsInEvent(Tickets tickets, Tickets oldtickets, Event event) throws Exception {eventTickets_DB.updateTicketInEvent(tickets, oldtickets, event);}
    public void deleteTicketsFromEvent(Tickets tickets, Event event) throws Exception {eventTickets_DB.removeTicketFromEvent(tickets, event);}
    public void deleteAllTicketsFromEvent(Event event) throws Exception {eventTickets_DB.deleteAllTicketFromEvent(event);}

//***************************HELPER*METHOD************************************
    public void removeImageFromTickets(Tickets ticket) throws Exception {
        String ticketJSON = ticket.getTicketJSON();
        JSONArray jsonArray = new JSONArray(ticketJSON);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if (jsonObject.has("id")) {
                String idString = jsonObject.getString("id");
                int numbers = Integer.parseInt(idString.replaceAll("[^0-9]+", ""));
                imageManager.deleteSystemIMG(numbers);
            }
        }
    }
}

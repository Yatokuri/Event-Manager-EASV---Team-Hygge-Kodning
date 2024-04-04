package bll;

import be.Tickets;
import dal.db.GlobalTickets_DB;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class GlobalTicketManager {

    private final GlobalTickets_DB globalTicketsDB;

    private ImageManager imageManager;

    public GlobalTicketManager() throws Exception {
        globalTicketsDB = new GlobalTickets_DB();
        imageManager = new ImageManager();
    }

    public List<Tickets> getAllGlobalTickets() throws Exception {
        return globalTicketsDB.getAllGlobalTicket();
    }

    public void addTicketToGlobal(Tickets ticketID) throws Exception {
        globalTicketsDB.addTicketToGlobal(ticketID);
    }

    public void removeTicketFromGlobal(Tickets ticketID) throws Exception {
        globalTicketsDB.removeTicketFromGlobal(ticketID);
    }

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

package be;

public class Tickets {
    private int ticketID;
    private int ticketQuantity;
    private String ticketName;
    private String ticketJSON;

    public Tickets(int ticketID, int ticketQuantity, String ticketName, String ticketJSON) {
        this.ticketQuantity = ticketQuantity;
        this.ticketName = ticketName;
        this.ticketJSON = ticketJSON;
        this.ticketID = ticketID;
    }

    public int getTicketID() {
        return ticketID;
    }

    public void setTicketID(int ticketID) {
        this.ticketID = ticketID;
    }

    public int getTicketQuantity() {
        return ticketQuantity;
    }

    public void setTicketQuantity(int ticketQuantity) {
        this.ticketQuantity = ticketQuantity;
    }

    public String getTicketName() {
        return ticketName;
    }

    public void setTicketName(String ticketName) {
        this.ticketName = ticketName;
    }

    public String getTicketJSON() {
        return ticketJSON;
    }

    public void setTicketJSON(String ticketJSON) {
        this.ticketJSON = ticketJSON;
    }

    @Override
    public String toString() {
        return "Tickets{" +
                "ticketID=" + ticketID +
                ", ticketQuantity=" + ticketQuantity +
                ", ticketName='" + ticketName + '\'' +
                ", ticketJSON='" + ticketJSON + '\'' +
                '}';
    }
}
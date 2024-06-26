package be;

public class Tickets {
    private int ticketID;
    private int ticketQuantity;
    private final String ticketName;
    private final String ticketJSON;
    private final int isILocal;

    public Tickets(int ticketID, int ticketQuantity, String ticketName, String ticketJSON, int isILocal) {
        this.ticketQuantity = ticketQuantity;
        this.ticketName = ticketName;
        this.ticketJSON = ticketJSON;
        this.ticketID = ticketID;
        this.isILocal = isILocal;
    }

    public int getIsILocal() {
        return isILocal;
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

    public String getTicketJSON() {
        return ticketJSON;
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
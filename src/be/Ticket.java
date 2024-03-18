package be;

public class Ticket {
    private String firstName, lastName, email;
    private int ticketID, ticketEventID, buyerID;

    public Ticket(String firstName, String lastName, String email, int ticketID, int ticketEventID, int buyerID){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.ticketID = ticketID;
        this.ticketEventID = ticketEventID;
        this.buyerID = buyerID;
    }

    public String getFirstName() {
        return firstName;
    }

    private void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    private void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    private void setEmail(String email) {
        this.email = email;
    }

    public int getTicketID() {
        return ticketID;
    }

    private void setTicketID(int ticketID) {
        this.ticketID = ticketID;
    }

    public int getBuyerID() {
        return buyerID;
    }

    public void setBuyerID(int buyerID) {
        this.buyerID = buyerID;
    }

    public int getTicketEventID() {
        return ticketEventID;
    }

    private void setTicketEventID(int ticketEventID) {
        this.ticketEventID = ticketEventID;
    }
}

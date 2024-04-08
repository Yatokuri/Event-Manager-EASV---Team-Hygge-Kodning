package be;

public class TicketSold {
    private String firstName, lastName, email;
    private int ticketID, transactionID;

    public TicketSold(String firstName, String lastName, String email, int ticketID, int transactionID){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.ticketID = ticketID;
        this.transactionID = transactionID;
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

    public int getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(int buyerID) {
        this.transactionID = buyerID;
    }

    //Custom message to the tbl view over sold ticket
    public String getCustomUserInfo() {
        return " " + firstName + " " + lastName ;
    }
}